
package org.wa9nnn.fdlog.javafx

object Callsign {

  private val callsignRegx = """(\p{Upper}{1,2})(\d)(\p{Upper}{1,3})""".r

  def isCallsign(text: String): Boolean = {
    callsignRegx.findFirstIn(text).isDefined
  }


}

