
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
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Route
import com.google.inject.Inject
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.model.{ContestProperty, NodeAddress}
import org.wa9nnn.fdcluster.store.StoreSender
import org.wa9nnn.webclient.{QsoLogger, SignOnOff}
import play.api.libs.json.{JsValue, Json}

import java.net.URL
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.{Failure, Success}

/**
 * An http server
 *
 * @param store    where qso and other dynamic info lives.
 * @param system   the actor system
 * @param config   from application.conf and command line.
 * @param nodeInfo who we are.
 */
@Singleton
class Server @Inject()(@Inject()
                       val store: StoreSender,
                       system: ActorSystem,
                       config: Config,
                       val aboutTable: AboutTable,
                       val contestProperty: ContestProperty,
                       val nodeAddress: NodeAddress,
                       val qsoLogger: QsoLogger,
                       val signOnOff: SignOnOff
                      ) extends UserRoutes with LazyLogging {
  private implicit val s = system
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val prettyPrint = config.getBoolean("fdcluster.prettyPrintJson")

  implicit def jsonToString(jsValue: JsValue): ToResponseMarshallable = {
    if (prettyPrint) {
      Json.prettyPrint(jsValue)
    } else {
      jsValue.toString()
    }
  }

  //#main-class
  // from the UserRoutes trait
  lazy val routes: Route = userRoutes
  //#main-class

  //#http-server
  private val url: URL = nodeAddress.url
  private val host: String = url.getHost
  private val port: Int = url.getPort
  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, host, port)


  serverBinding.onComplete {
    case Success(bound) =>
      logger.info(s"HTTP server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      logger.error(s"HTTP Server did not start!", e)
      system.terminate()
  }

  //  Await.result(system.whenTerminated, Duration.Inf)

}

