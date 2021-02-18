
package org.wa9nnn.util

import org.wa9nnn.util.InputHelper.{makeHappy, makeSad}
import scalafx.css.Styleable
import scalafx.scene.control.{TextField, TextFormatter}
import scalafx.util.converter.FormatStringConverter

import java.text.NumberFormat
import java.util.Locale

object InputHelper {
  /**
   *
   * @param textFields that will ensure uppercase
   */
  def forceCaps(textFields: TextField*): Unit = {
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
//     tf.textFormatter = new TextFormatter(converter)
      tf.setTextFormatter(new TextFormatter(converter))
//      tf.setTextFormatter(new TextFormatter[AnyRef]((change: TextFormatter.Change) => {
//        def foo(change: TextFormatter.Change) = {
//          change.setText(change.getText.toUpperCase)
//          change
//        }
//
//        foo(change)
//      }))
    }
  }

  def makeHappy(destination: Styleable): Boolean = {
    destination.styleClass.replaceAll("sadQso", "happyQso")
    true
  }

  def makeSad(destination: Styleable): Boolean = {
    destination.styleClass.replaceAll("happyQso", "sadQso")
    false
  }

}

trait HappySad {
  self: Styleable =>
  def happy(): Unit = {
    makeHappy(self)
  }

  def sad(): Unit = {
    makeSad(self)
  }
}
