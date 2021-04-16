
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

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.{Http, model}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.google.inject.name.Named
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.Markers.syncMarker
import org.wa9nnn.fdcluster.javafx.sync.{ResponseMessage, UuidsAtHost}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.QsosFromNode
import org.wa9nnn.fdcluster.store.network.FdHour
import play.api.libs.json.{JsObject, JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
 * send messages to another routes responce to a specfied actor i.e. Store or Cluster
 */
class HttpClientActor(@Named("store") store: ActorRef,
                      @Named("cluster") cluster: ActorRef,
                     ) extends Actor with LazyLogging {

  private implicit val materializer = Materializer.apply(context)

  private implicit val system: ActorSystem = context.system


  override def receive: Receive = {
    case sendable: Sendable ⇒
      logger.debug(syncMarker, s"Sending: $sendable")

      val request = sendable.httpRequest
      val responseFuture = Http().singleRequest(request)
      responseFuture
        .onComplete {
          case Success(httpResponse: model.HttpResponse) =>
            if (httpResponse.status.intValue() == 200) {
              val bytes: Source[ByteString, Any] = httpResponse.entity.dataBytes
              bytes.runFold(ByteString(""))(_ ++ _).foreach { body: ByteString =>
                val string = body.utf8String
                val js: JsObject = Json.parse(string).asInstanceOf[JsObject]

                val responseMessage: ResponseMessage = sendable.message.parseResponse(js)
                logger.debug(s"Response: $responseMessage dest: ${responseMessage.destination}")
                dispatch(responseMessage)
              }
            } else {
              logger.error(s"$request returned ${httpResponse.status}")
            }

          case Failure(et) =>
            logger.error(syncMarker, s"Failure", et)
        }

    //    case req: RequestUuidsForHour ⇒
    //      logger.debug(s"ClientActor got: $req")
    //      val responseFuture = Http().singleRequest(req.httpRequest)
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


/**
 *
 * @param fdHours empty gets all.
 */
case class RequestQsosForHours(fdHours: List[FdHour] = List.empty) extends JsonRequestResponse {
  override def toJson: JsObject = Json.toJson(this).as[JsObject]

  override def parseResponse(jsObject: JsObject): ResponseMessage = {
    jsObject.as[QsosFromNode]

  }
}

/**
 *
 * @param uuids empty gets all/
 */
case class RequestQsosForUuids(uuids: List[Uuid] = List.empty) extends JsonRequestResponse {
  override def toJson: JsObject = Json.toJson(this).as[JsObject]

  override def parseResponse(jsObject: JsObject): ResponseMessage = {
    jsObject.as[QsosFromNode]
  }

  override def toString: Node = s"RequestQsosForUuids for ${uuids.length} uuids"
}



