
package org.wa9nnn.util

import scalafx.scene.control.{TextField, TextFormatter, TextInputControl}
import scalafx.util.converter.FormatStringConverter

import java.text.NumberFormat

object InputHelper {
  /**
   *
   * @param textFields that will ensure uppercase
   */
  def forceCaps(textFields: TextInputControl*): Unit = {
    textFields.foreach {
      _.setTextFormatter(new TextFormatter[AnyRef]((change: TextFormatter.Change) => {
        def foo(change: TextFormatter.Change) = {
          change.setText(change.getText.toUpperCase)
          change
        }

        foo(change)
      }))
    }
  }
  /**
   *
   * @param textFields that will ensure integer only
   */
  def forceInt(textFields: TextField*): Unit = {
    val nf: NumberFormat = NumberFormat.getIntegerInstance()
    val converter: FormatStringConverter[Number] = new FormatStringConverter[Number](nf)

    textFields.foreach {tf =>
      tf.setTextFormatter(new TextFormatter(converter))
    }
  }


}


