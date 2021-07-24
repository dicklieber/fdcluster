package org.wa9nnn.util

import play.api.libs.json.{JsNull, JsNumber, JsString, JsValue}

import java.time.{Duration, Instant}

object JsonValueFactory {

  def apply(any: Any): JsValue = {
    if (any == null) {
      JsNull
    } else {
      any match {
        case s: String =>
          JsString(s)
        case duration: Duration =>
          JsString(duration.toString)
        case instant: Instant =>
          instant match {
            case Instant.MIN =>
              JsString("\u03B1")
            case Instant.MAX =>
              JsString("\u03C9")
            case _ =>
              JsString(instant.toString)
          }
        case i: Int =>
          JsNumber(BigDecimal(i))
        case d: Double =>
          JsNumber(BigDecimal(d))
        case l: Long =>
          JsNumber(BigDecimal(l))
        case None =>
          JsNull
        case Some(x) =>
          apply(x)
        case other =>
          JsString(other.toString)
      }
    }
  }
}
