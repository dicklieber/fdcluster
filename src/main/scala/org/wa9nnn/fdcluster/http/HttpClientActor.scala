
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

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.{Http, model}
import akka.stream.{Materializer, StreamTcpException}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.google.inject.name.Named
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.Markers.syncMarker
import org.wa9nnn.fdcluster.javafx.sync.{ResponseMessage, SendContainer}
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * HTTP client.  Sends messages, wrapped in a [[org.wa9nnn.fdcluster.javafx.sync.SendContainer]] to another node in the cluster.
 * @param store actor that manages QSOs
 * @param cluster actor that handles custer stuff.
 */
class HttpClientActor(@Named("store") store: ActorRef,
                      @Named("cluster") cluster: ActorRef,
                     ) extends Actor with LazyLogging {

  private implicit val materializer: Materializer = Materializer.apply(context)

  private implicit val system: ActorSystem = context.system

  override def receive: Receive = {
    case sendContainer: SendContainer ⇒

      val request = sendContainer.httpRequest
      val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
      responseFuture
        .onComplete {
          case Success(httpResponse: model.HttpResponse) =>
            if (httpResponse.status.intValue() == 200) {
              val bytes: Source[ByteString, Any] = httpResponse.entity.dataBytes
              bytes.runFold(ByteString(""))(_ ++ _).foreach { body: ByteString =>
                val string = body.utf8String
                val js: JsObject = Json.parse(string).asInstanceOf[JsObject]

                val responseMessage: ResponseMessage = sendContainer.message.parseResponse(js)
                logger.debug(s"Response: $responseMessage dest: ${responseMessage.destination}")
                dispatch(responseMessage)
              }
            } else {
              logger.error(s"$request returned ${httpResponse.status}")
            }

          case Failure(et) =>
            et match {
              case es:StreamTcpException =>
                val json = sendContainer.transactionId.toPrettyJson
                logger.error(es.getMessage + "\n" + json)
            }
            logger.error(syncMarker, s"Failure", et)
        }

    case x ⇒
      logger.error(s"Unexpected Message $x from $sender")
  }

  private def dispatch(responseMessage: ResponseMessage): Unit = {
    responseMessage.destination match {
      case DestinationActor.qsoStore =>
        store ! responseMessage
      case DestinationActor.cluster =>
        cluster ! responseMessage
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    logger.error(s"preRestart", reason)
    super.preRestart(reason, message)
  }
}




