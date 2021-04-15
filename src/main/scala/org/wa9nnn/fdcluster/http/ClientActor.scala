
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

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.{Http, model}
import akka.stream.Materializer
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.Markers.syncMarker
import org.wa9nnn.fdcluster.javafx.sync.{RequestUuidsForHour, SyncSteps, UuidsAtHost}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.QsosFromNode
import play.api.libs.json.Json

import java.net.URL
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class ClientActor(syncSteps: SyncSteps) extends Actor with LazyLogging {

  private implicit val materializer = Materializer.apply(context)

  private implicit val system: ActorSystem = context.system


  override def receive: Receive = {
    case sendable: Sendable[_] ⇒
      logger.debug(syncMarker, s"ClientActor got: $sendable")

      val request = sendable.httpRequest
      logger.debug(syncMarker, s"request:  $request")
      //      val start = Instant.now

      val responseFuture = Http().singleRequest(request)
      responseFuture
        .onComplete {
          case Success(httpResponse: model.HttpResponse) =>
            //            val contentType = entity.getContentType()
            //            val bytes: Source[ByteString, AnyRef] = entity.getDataBytes()
            if (httpResponse.status.intValue() == 200) {
              val bytes = httpResponse.entity.dataBytes
              bytes.runFold(ByteString(""))(_ ++ _).foreach { body: ByteString =>
                val string = body.utf8String
                val js = Json.parse(string)
                val qsosFromNode: UuidsAtHost = js.as[UuidsAtHost]
                //                val duration = Duration.between(start, Instant.now())
                val duration = "todo"
                syncSteps.step("Fetch", s"${qsosFromNode.uuids.size} in $duration")
                //                logger.debug(syncMarker, s"Got ${qsosFromNode.size}  qsorecord(s) from ${fa.url}")
                context.parent ! qsosFromNode
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

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    logger.error(s"preRestart", reason)
    super.preRestart(reason, message)
  }
}

object ClientActor {
  def props(syncSteps: SyncSteps): Props = Props(new ClientActor(syncSteps))
}

/**
 *
 * @param url  URL of best node.
 * @param path part of file part.
 */
case class FetchQsos(url: URL, path: String = "qsos")

object FetchQsos {
  val path: String = "qsos"

  def apply(url: URL) = new FetchQsos(url, path)
}

