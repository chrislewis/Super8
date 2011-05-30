package super8.model

import highchair.datastore.{Kind, Entity}
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.blobstore.BlobKey
import org.joda.time.DateTime

case class User(
  val key:      Option[Key],
  val email:    String,
  val password: String,
  val shows:    List[Key] /* Show Keys. */
) extends Entity[User]

object User extends Kind[User] {
  import org.apache.commons.codec.digest.DigestUtils.shaHex
  import com.google.appengine.api.datastore.DatastoreService
  
  val email     = property[String]("email")
  val password  = property[String]("password")
  val shows     = property[List[BlobKey]]("shows")
  val *         = email ~ password ~ shows
  
  val hashPass = shaHex((_:String))
  
  /** Override to hash passwords on save. */
  override def put(u: User)(implicit dss: DatastoreService) = 
    super.put(u.copy(password = shaHex(u.password)))
}


case class Show(
  val key:      Option[Key],
  val content:  BlobKey,
  val created:  DateTime,
  val views:    Long
) extends Entity[Show]

object Show extends Kind[Show] {
  def apply(blobKey: BlobKey): Show = Show(None, blobKey, new DateTime(), 0)
  
  val content = property[BlobKey]("content")
  val created = property[DateTime]("created")
  val views   = property[Long]("views")
  val *       = content ~ created ~ views
}
