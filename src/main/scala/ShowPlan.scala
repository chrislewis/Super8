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
      User where(_.email is u) and (_.password is User.hashPass(p)) fetchOne() map { user =>
        Body.bytes(req) >>: IO.write("text/html") fold (
          _ => InternalServerError,
          k =>
            (for {
              sk <- Show.put(Show(k)).key
              uk <- User.put(user.copy(shows = sk :: user.shows)).key
            } yield ResponseString(k.getKeyString)) getOrElse (InternalServerError)
        )
      } getOrElse(Unauthorized)
    
    case GET(Path(Seg("shows" :: key :: Nil))) =>
      key <<: IO.read fold (
        _                 => NotFound,
        { case (_, bytes) => Html(loadString(new String(bytes, "UTF-8"))) }
      )
    
    case GET(Path("/shows")) =>
      val list = Show.Kind2Query(Show) fetch(limit = 10) map { s =>
        <li><a href={"/shows/" + s.content.getKeyString}>{s.created}</a></li>
      }
      Html(<ul>{list}</ul>)
  }
}
