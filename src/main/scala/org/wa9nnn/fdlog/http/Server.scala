
package org.wa9nnn.fdlog.http

import java.net.URL

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Route
import com.google.inject.Inject
import com.google.inject.name.Named
import com.typesafe.config.Config
import org.wa9nnn.fdlog.store.NodeInfo
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.{Failure, Success}

class Server @Inject()(@Inject() @Named("store") val store: ActorRef, system: ActorSystem, config: Config, nodeInfo: NodeInfo) extends UserRoutes {
  private implicit val s = system
  implicit val executionContext: ExecutionContext = system.dispatcher

  private val prettyPrint = config.getBoolean("fdlog.prettyPrintJson")

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

