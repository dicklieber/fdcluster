
package org.wa9nnn.fdcluster.http

import java.net.URL
import java.time.{Duration, Instant}

import akka.actor.{Actor, Props}
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.{Http, model}
import akka.stream.Materializer
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.javafx.sync.{RequestUuidsForHour, ProgressStep}
import org.wa9nnn.fdcluster.model.QsosFromNode
import play.api.libs.json.Json
import scalafx.collections.ObservableBuffer
import org.wa9nnn.fdcluster.Markers.syncMarker
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.javafx.sync.StepsDataMethod.addStep
class ClientActor(stepsData: ObservableBuffer[ProgressStep]) extends Actor with LazyLogging {

  private implicit val materializer = Materializer.apply(context)

  private implicit val system = context.system

  override def receive: Receive = {
    case fa: FetchQsos ⇒
      logger.debug(syncMarker, s"ClientActor got: $fa")

      val request = fa.httpRequest
      logger.debug(syncMarker, s"request:  $request")
      val start = Instant.now

      val responseFuture = Http().singleRequest(request)
      responseFuture
        .onComplete {
          case Success(httpResponse: model.HttpResponse) =>
            //            val contentType = entity.getContentType()
            //            val bytes: Source[ByteString, AnyRef] = entity.getDataBytes()
            if(httpResponse.status.intValue() == 200) {
              val bytes = httpResponse.entity.dataBytes
              bytes.runFold(ByteString(""))(_ ++ _).foreach { body: ByteString =>
                val string = body.utf8String
                val js = Json.parse(string)
                val qsosFromNode: QsosFromNode = js.as[QsosFromNode]
                val duration = Duration.between(start, Instant.now())
                stepsData.step("Fetch", s"${qsosFromNode.size} in $duration")
                logger.debug(syncMarker, s"Got ${qsosFromNode.size}  qsorecord(s) from ${fa.url}")
                context.parent ! qsosFromNode
              }
            }else{
              logger.error(s"$request returned ${httpResponse.status}")
            }

          case Failure(et) =>
            logger.debug(syncMarker, s"Failure", et)
        }

    case req: RequestUuidsForHour ⇒
      logger.debug(s"ClientActor got: $req")
      val responseFuture = Http().singleRequest(req.httpRequest)
    case x ⇒
      logger.error(s"Unexpected Message $x from $sender")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    logger.error(s"preRestart", reason)
    super.preRestart(reason, message)
  }
}

object ClientActor {
  def props(stepsData: ObservableBuffer[ProgressStep]): Props = Props(new ClientActor(stepsData))
}

case class FetchQsos(url: URL, path: String = "qsos") extends HttpRequestGenerator {

}

object FetchQsos {
  val path: String = "qsos"

  def apply(url: URL): FetchQsos = new FetchQsos(url, path)
}

trait HttpRequestGenerator {
  val path: String
  val url: URL

  def httpRequest: HttpRequest = {
    HttpRequest(uri = Uri(url.toExternalForm)
      .withPath(Path("/" + path))
    )
  }


}