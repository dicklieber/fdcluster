
package org.wa9nnn.fdcluster.javafx.entry

import scalafx.scene.control.TextField


object ContestClass extends FieldValidator {

  private val regx = """(\d+)([IOH])""".r

  def valid(value: String): Option[String] = {
    if (regx.findFirstIn(value).isDefined)
      None
    else
      Option(errMessage)
  }

  override def errMessage = "e.g. 1H, 3O 2I"
}

object ContestSection extends FieldValidator {

  def valid(value: String): Option[String] = {
    if (Sections.byCode.contains(value))
      None
    else
      Option(errMessage)
  }

  override def errMessage = "ARRL Section"
}

object AlwaysValid extends FieldValidator {
  override def valid(value: String): Option[String] = None

  override def errMessage = ""

  override def valid(textField: TextField): Option[String] = None
}


trait FieldValidator {
  protected def errMessage: String

  /**
   *
   * @param value to validate
   * @return None if valid otherwise the error message.
   */
  def valid(value: String): Option[String]

  /**
   *
   * @param textField dto be validated.
   * @return one if valid otherwise the error message.
   */
  def valid(textField: TextField): Option[String] = {
    valid(textField.getText)
  }
}
case class EntryCategory(category:String) {
  val designator:Char = category.head
}

object EntryCategory {
  val catagories = Seq(
    EntryCategory("Home"),
    EntryCategory("Indoor"),
    EntryCategory("Outdoor"),
  )

  def forDesignator(designator:Char): Option[EntryCategory] = {
    catagories.find(_.designator == designator)
  }
}

