import org.wa9nnn.fdcluster.cabrillo._
import org.wa9nnn.fdcluster.model.MessageFormats._
import play.api.libs.json.Json

import scala.language.implicitConversions

val cb = CabrilloValues(Seq(CabrilloValue("n1", "v1"), CabrilloValue("n2", "v2")))
val sjson = Json.prettyPrint(Json.toJson(cb))
println(sjson)