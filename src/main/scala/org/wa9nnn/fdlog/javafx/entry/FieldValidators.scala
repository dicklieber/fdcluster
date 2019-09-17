
package org.wa9nnn.fdlog.javafx.entry

import scalafx.scene.control.TextField




object ContestClass extends FieldValidator {

  private val regx = """(\d+)(\p{Upper})""".r

  def valid(text: String): Boolean = {
    regx.findFirstIn(text).isDefined
  }
}

object ContestSection extends FieldValidator {
  private val regx = """(\d+)(\p{Upper})""".r

  def valid(text: String): Boolean = {
    Sections.byCode.contains(text)
  }
}

trait FieldValidator {
  def valid(fieldVlaue: String): Boolean

  def valid(textField: TextField): Boolean = {
    valid(textField.getText)
  }
}