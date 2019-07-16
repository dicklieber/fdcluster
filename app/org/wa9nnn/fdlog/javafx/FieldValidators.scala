
package org.wa9nnn.fdlog.javafx

object ContestCallsign extends ValidField {

  private val callsignRegx = """(\p{Upper}{1,2})(\d)(\p{Upper}{1,3})""".r

  def valid(text: String): Boolean = {
    callsignRegx.findFirstIn(text).isDefined
  }


}


object ContestClass extends ValidField {

  private val regx = """(\d+)(\p{Upper})""".r

  def valid(text: String): Boolean = {
    regx.findFirstIn(text).isDefined
  }
}
object ContestSection extends ValidField {

  private val regx = """(\d+)(\p{Upper})""".r

  def valid(text: String): Boolean = {
    regx.findFirstIn(text).isDefined
  }
}

trait ValidField {
  def valid(fieldVlaue: String):Boolean
}