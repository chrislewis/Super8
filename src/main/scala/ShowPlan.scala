package super8

import unfiltered.request._
import unfiltered.response._

import highchair.datastore.Connection.default
import scala.xml.XML.loadString
import io.IO
import model._

/** unfiltered plan */
class ShowPlan extends unfiltered.filter.Plan {
  def intent = {
    case req @ PUT(Path(Seg("shows" :: Nil)) & BasicAuth(u, p) & RequestContentType("text/html" :: xs)) =>
      User where(_.email is u) and (_.password is p) fetchOne() map { _ =>
        Body.bytes(req) >>: IO.write("text/html") fold (
          _ => InternalServerError,
          k => ResponseString(k.getKeyString)
        )
      } getOrElse(Unauthorized)
    
    case GET(Path(Seg("shows" :: key :: Nil))) =>
      key <<: IO.read fold (
        _                 => NotFound,
        { case (_, bytes) => Html(loadString(new String(bytes, "UTF-8"))) }
      )
    }
}
