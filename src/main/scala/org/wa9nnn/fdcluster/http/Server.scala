
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

import java.net.URL

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Route
import com.google.inject.Inject
import com.google.inject.name.Named
import com.typesafe.config.Config
import org.wa9nnn.fdcluster.store.NodeInfo
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.{Failure, Success}

/**
 * An http server
 * @param store where qso and other dynamic info lives.
 * @param system the actor system
 * @param config from application.conf and command line.
 * @param nodeInfo who we are.
 */
class Server @Inject()(@Inject() @Named("store") val store: ActorRef,
                       system: ActorSystem,
                       config: Config,
                       val nodeInfo: NodeInfo) extends UserRoutes {
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
  private val url: URL = nodeInfo.url
  private val host: String = url.getHost
  private val port: Int = url.getPort
  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, host, port)


  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  //  Await.result(system.whenTerminated, Duration.Inf)

}

