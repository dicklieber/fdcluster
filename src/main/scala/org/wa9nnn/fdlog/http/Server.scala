
package org.wa9nnn.fdlog.http

import java.net.URL

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.google.inject.Inject
import com.google.inject.name.Named
import com.typesafe.config.Config
import org.wa9nnn.fdlog.store.NodeInfo

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

class Server @Inject()(@Inject() @Named("store") val store: ActorRef,  system: ActorSystem, config: Config, nodeInfo: NodeInfo) extends UserRoutes {
  implicit val s = system
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher


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
