package org.wa9nnn.fdcluster.model

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}

import scala.language.implicitConversions
import scala.util.matching.Regex


case class CallSign(callSign: String) extends Ordered[CallSign] {
  override def compare(that: CallSign): Int = {
    callSign compareTo (that.callSign)
  }
  def isEmpty:Boolean = false
}

object CallSign {
  val regex: Regex = """[a-zA-Z0-9]{1,3}[0123456789][a-zA-Z0-9]{0,3}/?[a-zA-Z0-9]*(:?/.*)?""".r

  def parse(cs: String): CallSign = {
    val matches: Boolean = regex.matches(cs)
    if (matches) {
      new CallSign(cs)
    } else {
      throw new MatchError(cs)
    }
  }

  implicit def s2cs(s: String): CallSign = {
    CallSign(s)
  }
  implicit def cs2s(s: CallSign): String = {
    s.callSign
  }


  /**
   * to make JSON a bit more compact
   */
  implicit val callSignFormat: Format[CallSign] = new Format[CallSign] {
    override def reads(json: JsValue): JsResult[CallSign] = {
      val cs: String = json.as[String]
      try {
        JsSuccess(CallSign(cs))
      }
      catch {
        case e: IllegalArgumentException ⇒ JsError(e.getMessage)
      }
    }

    override def writes(callSign: CallSign): JsValue = {
      JsString(callSign.toString)
    }
  }
}


