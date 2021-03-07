
package org.wa9nnn.fdcluster.javafx

import org.wa9nnn.fdcluster.javafx.entry.FieldValidator

object CallsignValidator extends FieldValidator {

  private val callsignRegx =
    """(\p{Upper}{1,2})(\d)(\p{Upper}{1,3})""".r

  def valid(value: String): Option[String] = {
    if (callsignRegx.findFirstIn(value).isDefined)
      None
    else
      Some(errMessage)
  }

  override def errMessage: String = "Not callsign!"
}
