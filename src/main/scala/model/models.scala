package super8.model

import highchair.datastore._
import com.google.appengine.api.datastore.Key

case class User(
  val key: Option[Key],
  val email: String,
  val password: String
) extends Entity[User]

object User extends Kind[User] {
  import org.apache.commons.codec.digest.DigestUtils.shaHex
  import com.google.appengine.api.datastore.DatastoreService
  
  val email     = property[String]("email")
  val password  = property[String]("password")
  val *         = email ~ password
  
  /** Override to hash passwords on save. */
  override def put(u: User)(implicit dss: DatastoreService) = 
    super.put(u.copy(password = shaHex(u.password)))
}
