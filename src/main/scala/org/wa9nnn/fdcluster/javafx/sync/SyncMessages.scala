
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
import org.wa9nnn.fdcluster.model.{NodeAddress, Qso}
import org.wa9nnn.fdcluster.store.network.FdHour
import play.api.libs.json.{JsObject, Json}

import java.time.Instant

/**
 *
 * @param fdHour empty for all FdHours
 */
case class RequestUuidsForHour(fdHour: FdHour) extends Sendable {
  def jsObject: JsObject = Json.toJson(this).as[JsObject]

  def className: String = getClass.getName

  def parseResponse(jsObject: JsObject): ResponseMessage = {
    jsObject.as[UuidsAtHost]
  }
}

object RequestUuidsForHour {
  def apply(fdHour: FdHour, destination: NodeAddress, origin: NodeAddress, clazz: Class[_]): RequestUuidsForHour = {
    new RequestUuidsForHour(fdHour)
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
}

trait Sendable {
  def className: String

  def path: String = ClassToPath(className)

  def jsObject: JsObject

  def parseResponse(jsObject: JsObject): ResponseMessage
}


/**
 *
 * @param nodeAddress where this came from. //TODO do we need this
 * @param uuids       on this node for rested FdHours (or all)
 */
case class UuidsAtHost(nodeAddress: NodeAddress, uuids: List[Uuid]) extends UuidContainer {
  override def toString: Node = f"${uuids.length} uuids from $nodeAddress"
}

case class QsosFromNode(qsos: List[Qso]) extends ResponseMessage with JsonRequestResponse {
  def size: Int = qsos.size

  override def toString: String = s"QsosFromNode for ${qsos.length} qsos"

  override val destination: DestinationActor = DestinationActor.qsoStore

  override def parseResponse(jsObject: JsObject): ResponseMessage = {
    jsObject.as[QsosFromNode]
  }

  override def toJson: JsObject =
    Json.toJson(this).asInstanceOf[JsObject]
}

case class RequestQsosForUuids(uuids: List[Uuid] = List.empty) extends Sendable {
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
case class RequestQsosForHour(fdHour: FdHour) extends Sendable {

  override def parseResponse(jsObject: JsObject): ResponseMessage = {
    jsObject.as[QsosFromNode]
  }

  override def className: String = getClass.getName

  override def jsObject: JsObject = Json.toJson(this).asInstanceOf[JsObject]
}

object RequestQsosForHour {
  def apply(fdHour: FdHour, destination: NodeAddress, origin: NodeAddress, clazz: Class[_]): RequestQsosForHour = {
    new RequestQsosForHour(fdHour)
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

