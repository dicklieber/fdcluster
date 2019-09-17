
package org.wa9nnn.fdlog.javafx

import org.wa9nnn.fdlog.javafx.entry.FieldValidator

object ContestCallsign extends FieldValidator {

  private val callsignRegx = """(\p{Upper}{1,2})(\d)(\p{Upper}{1,3})""".r

  def valid(text: String): Boolean = {
    callsignRegx.findFirstIn(text).isDefined
  }
}
