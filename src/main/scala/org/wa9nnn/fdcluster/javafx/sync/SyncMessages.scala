
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

package org.wa9nnn.fdcluster.javafx.sync

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{ContentTypes, HttpRequest}
import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.http.DestinationActor
import org.wa9nnn.fdcluster.model.MessageFormats.{Uuid, _}
import org.wa9nnn.fdcluster.model.{NodeAddress, QsoRecord}
import org.wa9nnn.fdcluster.store.network.FdHour
import play.api.libs.json.{JsObject, Json}

import java.time.Instant

/**
 *
 * @param fdHour empty for all FdHours
 */
case class RequestUuidsForHour(fdHour: FdHour, transactionId: TransactionId) extends Sendable {
  def jsObject: JsObject = Json.toJson(this).as[JsObject]

  def className: String = getClass.getName

  def parseResponse(jsObject: JsObject): ResponseMessage = {
    jsObject.as[UuidsAtHost]
  }
}

object RequestUuidsForHour {
  def apply(fdHour: FdHour, destination: NodeAddress, origin: NodeAddress, clazz: Class[_]): RequestUuidsForHour = {
    new RequestUuidsForHour(fdHour,
      TransactionId(destination,
        origin,
        fdHour,
        List(Step(clazz)))
    )
  }
}

case class SendContainer(message: Sendable, otherNode: NodeAddress) {
  def httpRequest: HttpRequest = {
    HttpRequest(uri = otherNode.uri
      .withScheme("http")
      .withPath(Path("/" + message.path)))
      .withEntity(ContentTypes.`application/json`, Json.prettyPrint(message.jsObject))
      .withMethod(akka.http.scaladsl.model.HttpMethods.POST)
  }

  def transactionId: TransactionId = message.transactionId
}

trait Sendable {
  def className: String

  def path: String = ClassToPath(className)

  def transactionId: TransactionId

  def jsObject: JsObject

  def parseResponse(jsObject: JsObject): ResponseMessage
}


/**
 *
 * @param nodeAddress where this came from. //TODO do we need this
 * @param uuids       on this node for rested FdHours (or all)
 */
case class UuidsAtHost(nodeAddress: NodeAddress, uuids: List[Uuid], transactionId: TransactionId) extends UuidContainer {
  override def toString: Node = f"${uuids.length} uuids from $nodeAddress"
}

case class QsosFromNode(qsos: List[QsoRecord], transactionId: TransactionId) extends ResponseMessage with JsonRequestResponse {
  def size: Int = qsos.size

  override def toString: String = s"QsosFromNode for ${qsos.length} qsos"

  override val destination: DestinationActor = DestinationActor.qsoStore

  override def parseResponse(jsObject: JsObject): ResponseMessage = {
    jsObject.as[QsosFromNode]
  }

  override def toJson: JsObject =
    Json.toJson(this).asInstanceOf[JsObject]
}

case class RequestQsosForUuids(uuids: List[Uuid] = List.empty, transactionId: TransactionId) extends Sendable {
  //  def toJson: JsObject = Json.toJson(this).as[JsObject]

  override def parseResponse(jsObject: JsObject): ResponseMessage = {
    jsObject.as[QsosFromNode]
  }

  override def toString: String = s"RequestQsosForUuids for ${uuids.length} uuids"

  override def className: String = getClass.getName

  override def jsObject: JsObject = Json.toJson(this).as[JsObject]
}

trait UuidContainer extends ResponseMessage {
  val uuids: List[Uuid]

  def iterator: Iterator[Uuid] = {
    uuids.iterator
  }

  val destination = DestinationActor.cluster
}

trait ResponseMessage {
  val destination: DestinationActor
}
/**
 *
 * @param fdHour of interest..
 */
case class RequestQsosForHour(fdHour: FdHour, transactionId: TransactionId) extends Sendable {

  override def parseResponse(jsObject: JsObject): ResponseMessage = {
    jsObject.as[QsosFromNode]
  }

  override def className: String = getClass.getName

  override def jsObject: JsObject = Json.toJson(this).asInstanceOf[JsObject]
}

object RequestQsosForHour {
  def apply(fdHour: FdHour, destination: NodeAddress, origin: NodeAddress, clazz: Class[_]): RequestQsosForHour = {
    new RequestQsosForHour(fdHour,
      TransactionId(destination,
        origin,
        fdHour,
        List(Step(clazz)))
    )
  }
}

/**
 * Collects info about each step in syncing on [[FdHour]] thru sever back and forth between  client host and serer host.
 *
 * @param otherNode client where this started
 * @param thisNode  server where we're syncing from.
 * @param fdHour    which block this.
 * @param steps     each step along the way.
 * @param start     when the transaction started.
 */
case class TransactionId private(otherNode: NodeAddress,
                                 thisNode: NodeAddress,
                                 fdHour: FdHour,
                                 steps: List[Step], start: Instant = Instant.now()) extends LazyLogging  {
  def duration: java.time.Duration = java.time.Duration.between(start, Instant.now())

  def addStep(clazz: Class[_]): TransactionId = {
    val transactionId = copy(steps = steps.appended(Step(clazz)))
    logger.info(transactionId.toString)
    transactionId
  }

  def toPrettyJson: String = Json.prettyPrint(Json.toJson(this))

  override def toString: Node = {
    val r = steps.foldLeft(s"$otherNode ==> $thisNode for $fdHour") { (accum, step) =>
      "\n" + accum + step.toString
    }
    r
  }
}

case class Done(transactionId: TransactionId) extends ResponseMessage {
  override val destination: DestinationActor = DestinationActor.cluster
}

object TransactionId {
  def apply(otherNode: NodeAddress,
            thisNode: NodeAddress,
            fdHour: FdHour,
            clazz: Class[_]): TransactionId = {
    new TransactionId(otherNode, thisNode, fdHour, List(Step(clazz)))
  }
}


case class Step(name: String, instant: Instant = Instant.now())

object Step {
  def apply(clazz: Class[_]): Step = {
    new Step(clazz.getName)
  }
}

trait JsonRequestResponse extends Product {
  def name: String = super.getClass.getName

  def parseResponse(jsObject: JsObject): ResponseMessage

  def toJson: JsObject

}


/**
 * Determine Uir path name from a class name.
 * This allows the name of the class (actually the last part without package name)
 */
object ClassToPath {
  def apply(clazz: Class[_]): String = {
    val name = clazz.getName
    apply(name)
  }

  def apply(name: String): String = {
    val className: Node = name.split("""\.""").last
    className.head.toLower + className.tail
  }

}

