package org.wa9nnn.fdcluster.http

import akka.actor.ActorRef
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, Uri}
import org.wa9nnn.fdcluster.javafx.sync.ResponseMessage
import org.wa9nnn.fdcluster.model.MessageFormats._
import play.api.libs.json.{JsObject, JsValue, Json}

/**
 * A container for something that can be sent to another node via HTTP.
 *
 * @param message  what to send.
 * @param uri      where to send to.
 * @param consumer who gets the response
 */
case class Sendable(message: JsonRequestResponse, uri: Uri, consumer: ActorRef) {
  val last: String = ClassToPath(message.name)


  def httpRequest: HttpRequest = {
    HttpRequest(uri = uri
      .withScheme("http")
      .withPath(Path("/" + last)))
      .withEntity(ContentTypes.`application/json`, Json.prettyPrint(message.toJson))
      .withMethod(akka.http.scaladsl.model.HttpMethods.POST)
  }
}

trait JsonRequestResponse extends Product {
  def name: String = super.getClass.getName

  def parseResponse(jsObject: JsObject):ResponseMessage

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