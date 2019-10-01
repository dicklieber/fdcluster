
package org.wa9nnn.fdlog.store.network.cluster

import java.net.URL

import akka.actor.{Actor, Props}
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.{Http, model}
import akka.stream.Materializer
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdlog.Markers.syncMarker
import org.wa9nnn.fdlog.javafx.sync.Step
import org.wa9nnn.fdlog.model.MessageFormats._
import org.wa9nnn.fdlog.model.QsoRecord
import play.api.libs.json.Json
import scalafx.collections.ObservableBuffer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import org.wa9nnn.fdlog.javafx.sync.StepsDataMethod.addStep


class ClientActor(stepsData:ObservableBuffer[Step]) extends Actor with LazyLogging {

  private implicit val materializer = Materializer.apply(context)

  private implicit val system = context.system

  override def receive: Receive = {
    case fa: FetchQsos ⇒
      logger.debug(syncMarker, s"ClientActor got: $fa")

      val request = fa.request
      logger.debug(syncMarker, s"request:  $request")

      val responseFuture = Http().singleRequest(request)
      responseFuture
        .onComplete {
          case Success(httpResponse: model.HttpResponse) =>
            //            val contentType = entity.getContentType()
//            val bytes: Source[ByteString, AnyRef] = entity.getDataBytes()
            httpResponse.entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body: ByteString =>
              val string = body.utf8String
              val js = Json.parse(string)
              val qsoRecords: Seq[QsoRecord] =  js.as[Seq[QsoRecord]]
              logger.debug(syncMarker, s"Got ${qsoRecords.size}  qsorecord(s) from ${fa.url}")
              context.parent ! qsoRecords
            }

          case Failure(et) =>
            logger.debug(syncMarker, s"Failure", et)
        }
    case x ⇒
      logger.error(s"Unexpected Message $x from $sender")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    logger.error(s"preRestart", reason)
    super.preRestart(reason, message)
  }
}

object ClientActor {
  def props(stepsData:ObservableBuffer[Step]):Props = Props(new ClientActor(stepsData))
}
case class FetchQsos(url: URL, path: String = "qsos") {

  def request: HttpRequest = {
    HttpRequest(uri = Uri(url.toExternalForm)
      .withPath(Path("/" + path))
    )
  }
}

object FetchQsos {
  val path: String = "qsos"

  def apply(url: URL): FetchQsos = new FetchQsos(url, path)
}
