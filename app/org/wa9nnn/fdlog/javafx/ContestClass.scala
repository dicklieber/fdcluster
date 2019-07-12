
package org.wa9nnn.fdlog.javafx

object ContestClass {

  private val regx = """(\d+)(\p{Upper})""".r

  def isClass(text: String): Boolean = {
    regx.findFirstIn(text).isDefined
  }
}
