
package org.wa9nnn.fdcluster.javafx.entry

import scalafx.beans.property.StringProperty
import scalafx.scene.control.TextInputControl


object ContestClassValidator extends FieldValidator {

  private val regx = """(\d+)([IOH])""".r

  def valid(value: String): Option[String] = {
    if (regx.findFirstIn(value).isDefined)
      None
    else
      Option(errMessage)
  }

  override def errMessage = "e.g. 1H, 3O 2I"
}

object ContestSectionValidator extends FieldValidator {

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

  override def valid(textField: TextInputControl): Option[String] = None
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
   * @param textInputControl to be validated. [[scalafx.scene.control.TextField]] or [[scalafx.scene.control.TextArea]]
   * @return one if valid otherwise the error message.
   */
  def valid(textInputControl: TextInputControl): Option[String] = {
    valid(textInputControl.getText)
  }
  /**
   *
   * @param stringProperty to be validated.
   * @return one if valid otherwise the error message.
   */
  def valid(stringProperty: StringProperty): Option[String] = {
    valid(stringProperty.value)
  }
}

case class EntryCategory(category: String) {
  val designator: Char = category.head

  def buildClass(transmitters: Int): String = s"$transmitters$designator"
}

object EntryCategory {
  def fromEntryClass(entryClass: String): EntryCategory = {
    forDesignator(entryClass.head)
  }

  val catagories = Seq(
    EntryCategory("Home"),
    EntryCategory("Indoor"),
    EntryCategory("Outdoor"),
  )
  val defaultCategory: EntryCategory = catagories(2)

  def forDesignator(designator: Char): EntryCategory = {
    catagories.find(_.designator == designator).getOrElse(defaultCategory)
  }
}

