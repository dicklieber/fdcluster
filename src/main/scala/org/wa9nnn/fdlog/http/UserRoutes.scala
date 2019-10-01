package org.wa9nnn.fdlog.http

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdlog.model.MessageFormats._
import org.wa9nnn.fdlog.model.QsoRecord
import org.wa9nnn.fdlog.model.sync.QsoHour
import org.wa9nnn.fdlog.store.StoreActor.DumpQsos
import org.wa9nnn.fdlog.store.network.FdHour
import org.wa9nnn.fdlog.store.network.cluster.FetchQsos
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._
import scala.language.postfixOps

trait UserRoutes extends LazyLogging {
  /**
   * Automatically applied to convert the JsValue, e.g. {{Json.toJson(qsoHours)}} to what complete() needs.
   * complete(Json.toJson(qsoHours))
   * @param jsValue e.g. Json.toJson(x)
   * @return either a JSON string or a pretty-printed json string.
   */
  implicit  def jsonToString(jsValue:JsValue) :ToResponseMarshallable

  val store: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5 seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val userRoutes: Route =
  encodeResponse(
    get {
      concat(
        pathSingleSlash {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body>todo add help for API!</body></html>"))
        },
        path("qsoHours") {
          val dummyFdHour = FdHour.allHours
          onSuccess((store ? dummyFdHour).mapTo[Seq[QsoHour]]) { qsoHours: Seq[QsoHour] ⇒
            complete(Json.toJson(qsoHours))
          }
        },
        path(FetchQsos.path) {
          onSuccess((
            store ? DumpQsos
            ).mapTo[Seq[QsoRecord]]) { qsoHours: Seq[QsoRecord] ⇒
            logger.debug(s"FetchQsos: ")
            complete(Json.toJson(qsoHours))
          }
        },
      )
    }
  )
}
