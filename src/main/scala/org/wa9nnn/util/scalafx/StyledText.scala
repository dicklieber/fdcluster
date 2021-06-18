package org.wa9nnn.util.scalafx

import scalafx.css.Styleable
import scalafx.scene.control.{Label, Labeled}
import scalafx.scene.layout.Pane

case class StyledText(text: String, cssStyle: String*) {
  def applyToLabel(control: Label): Unit = {
    control.text = text
    control.styleClass = cssStyle.toIterable
  }
}

/**
 * Helper to set value and styleClass.
 * Removes  old styles to avoid leaking styles.
 *
 * @param cssStyles choices
 */
 class LabeledHelper(cssStyles: String*) {
  /**
   *
   * @param labeled   to set
   * @param value     to set to/
   * @param cssStyle  must be one from [[cssStyles]]
   */
  def apply(labeled: Labeled, value: String, cssStyle: String): Unit = {
    assert(cssStyles.contains(cssStyle), s"Must be one of ${cssStyles.mkString("")}")
    labeled.text = value
    val styleClass = labeled.styleClass
    styleClass --= cssStyles
    styleClass += cssStyle
  }
  /**
   *
   * @param pane   to set
   * @param cssStyle  must be one from [[cssStyles]]
   */
  def apply(pane: Styleable, cssStyle: String): Unit = {
    assert(cssStyles.contains(cssStyle), s"Must be one of ${cssStyles.mkString("")}")
    val styleClass = pane.styleClass
    styleClass -- cssStyles
    styleClass += cssStyle
    println(styleClass)
  }
}

object HappySad extends LabeledHelper("sad", "happy"){
  def happy(labeled: Labeled,  value: String): Unit = {
    apply(labeled, value, "happy")
  }

  def sad(labeled: Labeled, value: String): Unit = {
    apply(labeled, value,  "sad")
  }
  def happy(pane: Styleable): Unit = {
    apply(pane, "happy")
  }

  def sad(pane: Styleable): Unit = {
    apply(pane,  "sad")
  }
}

object StyledText {
  def apply(): StyledText = new StyledText("")

  def sad(text: String): StyledText = {
    new StyledText(text, "sad")
  }
}
