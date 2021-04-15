package org.wa9nnn.fdcluster.http

import akka.http.scaladsl.model.{ContentTypes, HttpMethod, HttpRequest, Uri}
import akka.http.scaladsl.model.Uri.Path
import play.api.libs.json.{JsValue, Json, Writes}

import scala.reflect.{ClassTag, classTag}

/**
 * Something that can be sent to another node via HTTP.
 *
 * @param message what to send.
 * @param uri     where to send to.
 */
case class Sendable[T <: Product : ClassTag](message: T, uri: Uri) (implicit tjs: Writes[T]){
  val jsValue: JsValue = Json.toJson(message)
  private val name = classTag[T].runtimeClass.getName
  val last: String = name.split("""\.""").last

  def httpRequest: HttpRequest = {
    HttpRequest(uri = uri
      .withScheme("http")
      .withPath(Path("/" + last)))
      .withEntity(ContentTypes.`application/json`, Json.prettyPrint(jsValue))
      .withMethod(akka.http.scaladsl.model.HttpMethods.POST)
  }
}