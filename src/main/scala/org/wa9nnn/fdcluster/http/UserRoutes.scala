/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.fdcluster.http

import about.AboutTable
import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.html
import org.wa9nnn.fdcluster.javafx.sync._
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.sync.NodeStatus
import org.wa9nnn.fdcluster.model.{ContestProperty, NodeAddress}
import org.wa9nnn.fdcluster.store.{RequestNodeStatus, StoreSender}
import org.wa9nnn.webclient.{QsoLogger, SignOnOff}
import play.api.libs.json.JsValue

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

//todo mmake this an injectable clas
trait UserRoutes extends LazyLogging {

  import PlayJsonSupport._

  val nodeAddress: NodeAddress
  val aboutTable: AboutTable
  val contestProperty: ContestProperty
  val qsoLogger: QsoLogger
  val signOnOff: SignOnOff

  /**
   * Automatically applied to convert the JsValue, e.g. {{Json.toJson(qsoHours)}} to what complete() needs.
   * complete(Json.toJson(qsoHours))
   *
   * @param jsValue e.g. Json.toJson(x)
   * @return either a JSON string or a pretty-printed json string.
   */
  implicit def jsonToString(jsValue: JsValue): ToResponseMarshallable

  val store: StoreSender

  // Required by the `ask` (?) method below
  implicit lazy val timeout: Timeout = Timeout(5 seconds) // usually we'd obtain the timeout from the system's configuration

  def requestMethod(req: HttpRequest): String = req.method.name


  lazy val userRoutes: Route =

    encodeResponse(
      logRequestResult("overall")(

        concat(
          get {
            concat(
              pathSingleSlash {
                complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, {
                  html.Landing().toString()
                }
                ))
              },
            )
          }, get {

            concat(
              // logs just the request method and response status at info level

              path("about") {
                complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, {
                  val table = aboutTable()
                  html.AboutDialog(table).toString()
                }
                ))

              },
              path("nodeStatus") {
                onSuccess((
                  store ?[NodeStatus] RequestNodeStatus
                  ).mapTo[NodeStatus]) { nodeStatus ⇒
                  complete {
                    nodeStatus
                  }
                }

              },
              path("contestImage") {
                val imagePath: String = s"images/${contestProperty.contestName}.png"
                getFromResource(imagePath)
              },
              pathPrefix("images") {
                getFromResourceDirectory("images")
              },
              pathPrefix("css") {
                getFromResourceDirectory("css")
              },
              pathPrefix("javascripts") {
                getFromResourceDirectory("javascripts")
              },
              qsoLogger.qsoEntryRoute,
              signOnOff.signonRoute,
              signOnOff.logOutRoute,
              qsoLogger.possibleDupRoute,
            )
          },
          post {

            concat(

              signOnOff.doSignonRoute,
              signOnOff.changeStation,
              path("nodeStatusRequest") {
                onSuccess((
                  store ?[NodeStatus] RequestNodeStatus
                  ).mapTo[NodeStatus]) { nodeStatus ⇒
                  complete {
                    nodeStatus
                  }
                }

              },
              path({
                val str = ClassToPath(classOf[RequestUuidsForHour])
                str
              }) {
                val um = as[RequestUuidsForHour]
                entity(um) { uuidRequest ⇒
                  onSuccess((
                    store ? uuidRequest
                    ).mapTo[UuidsAtHost]) { uuids: UuidsAtHost ⇒
                    complete {
                      uuids
                    }
                  }
                }
              }
              ,
              path(ClassToPath(classOf[RequestQsosForUuids])) {
                val um = as[RequestQsosForUuids]
                entity(um) { uuidRequest ⇒
                  onSuccess((
                    store ? uuidRequest
                    ).mapTo[QsosFromNode]) { qsosFromNode ⇒
                    complete {
                      qsosFromNode
                    }
                  }
                }
              },
              path(ClassToPath(classOf[RequestQsosForHour])) {
                val um = as[RequestQsosForHour]
                entity(um) { qsoRequest ⇒
                  onSuccess((
                    store ?[QsosFromNode] qsoRequest
                    ).mapTo[QsosFromNode]) { qsosFromNode ⇒
                    complete {
                      qsosFromNode
                    }
                  }
                }
              },
              qsoLogger.logQsoRoute
              //            Xyzzy.apply
              //            path("LogQso") {
              //              formFields("callSign", "class", "section") { (callSign, clas, section) =>
              //                complete(s"Please log $callSign $clas $section")
              //              }
              //            }
            )
          }
        )
      )
    )

}

