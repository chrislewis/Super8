package super8.io

object IO {
  import com.google.appengine.api.files.{
    AppEngineFile,
    FileService,
    FileServiceFactory => FSF,
    FileWriteChannel
  }
  import com.google.appengine.api.blobstore.{
    BlobInfo,
    BlobInfoFactory,
    BlobKey
  }
  import java.io.{
    BufferedReader,
    IOException
  }
  import java.nio.channels.Channels
  import scala.util.control.Exception.catching
  
  val catcher = catching(classOf[IOException])
  
  def file(fs: FileService)(m: String): Either[Throwable, AppEngineFile] =
    catcher either fs.createNewBlobFile(m)
    
  def writeChannel(fs: FileService)(f: AppEngineFile): Either[Throwable, (AppEngineFile, FileWriteChannel)] =
    catcher either (f, fs.openWriteChannel(f, true))
  
  def writeBytes(pair: (AppEngineFile, FileWriteChannel))(bytes: Array[Byte]): Either[Throwable, AppEngineFile] = pair match {
    case (f, ch) =>
      try {
        Channels.newOutputStream(ch).write(bytes)
        Right(f)
      } catch {
        case e: Exception => Left(e)
      } finally {
        ch.closeFinally()
      }
  }
  
  class Writer(mimeType: String) {
    val fs = FSF.getFileService
    
    def >>:(bytes: Array[Byte]): Either[Throwable, BlobKey] =
      file(fs)(mimeType)
        .fold (Left(_), writeChannel(fs)(_)
          .fold (Left(_), writeBytes(_)(bytes)
            .fold (Left(_), f => Right(fs.getBlobKey(f)
        ))))
    
    /*def >>:(bytes: Array[Byte]): Either[Exception, BlobKey] = {
      val gaeFile = fs.createNewBlobFile(mimeType)      // IOException
      val channel = fs.openWriteChannel(gaeFile, true)  // FinalizationException, LockException
      val out = Channels.newOutputStream(channel)
      out.write(bytes)                                  // IOException
      channel.closeFinally()                            // IOException
      Right(fs.getBlobKey(gaeFile))
    }*/
  }
  
  class Reader() {
    val fs = FSF.getFileService
    def <<:(key: String): Either[Exception, (BlobInfo, Array[Byte])] = {
      val bkey = new BlobKey(key)
      val file = fs.getBlobFile(bkey)
      // FIXME super hack
      val dss = com.google.appengine.api.datastore.DatastoreServiceFactory.getDatastoreService()
      val bif = new BlobInfoFactory(dss)
      // Can use BlobInfo to allocate a full memory buffer
      val info = bif.loadBlobInfo(bkey)
      
      val channel = fs.openReadChannel(file, false)
      
      val is = new java.io.BufferedInputStream(Channels.newInputStream(channel))
      val buf = new Array[Byte](2048)
      val lbuf = new scala.collection.mutable.ListBuffer[Byte]
      var read: Int = -1
      while({ read = is.read(buf); read > -1 }) {
        lbuf ++= buf.view(0, read).toList
      }
      
      channel.close()
      Right(info -> lbuf.toArray)
    }
  }
  
  def write(mimeType: String) = new Writer(mimeType)
  
  def read = new Reader()
}
