
package org.wa9nnn.fdcluster.adif

/**
 * Things the the [[AdifParser]] send to callback.
 */
sealed trait AdifResult {

}

object AdifResult {
  val eoh: AdifSeperator = AdifSeperator("EOH")
  val eor: AdifSeperator = AdifSeperator("EOR")
}

case class AdifEntry(predef: String, tag: String, value: String) extends AdifResult

case class AdifSeperator(name: String) extends AdifResult

case class AdifError(exception: Exception) extends AdifResult
