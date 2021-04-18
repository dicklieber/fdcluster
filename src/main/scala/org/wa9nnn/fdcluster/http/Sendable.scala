package org.wa9nnn.fdcluster.http

import org.wa9nnn.fdcluster.javafx.sync.JsonRequestResponse
import org.wa9nnn.fdcluster.model.MessageFormats._
//
///**
// * A container for something that can be sent to another node via HTTP.
// *
// * @param message  what to send.
// * @param uri      where to send to.
// * @param consumer who gets the response
// */
//case class Sendable[T <: Product : ClassTag](message: Product, uri: Uri, consumer: ActorRef) (implicit tjs: Writes[T]){{
//
//  val jsValue: JsValue = Json.toJson(message)
//  private val name = classTag[T].runtimeClass.getName
//  val last: String = name.split("""\.""").last
//
////  val last: String = ClassToPath(message.getClass)
////  private val name = classTag[T].runtimeClass.getName
////  val jsValue: JsValue = Json.toJson(message)
//
//
//  def httpRequest: HttpRequest = {
//    HttpRequest(uri = uri
//      .withScheme("http")
//      .withPath(Path("/" + last)))
//      .withEntity(ContentTypes.`application/json`, Json.prettyPrint(jsValue))
//      .withMethod(akka.http.scaladsl.model.HttpMethods.POST)
//  }
//}

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, Uri}
import play.api.libs.json.{JsValue, Json, Writes}

import scala.reflect.{ClassTag, classTag}




