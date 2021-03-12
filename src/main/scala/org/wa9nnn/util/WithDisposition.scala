
package org.wa9nnn.util

import scalafx.css.Styleable

/**
 * A Control (actually any [[Styleable]]) that can be happy or sad.
 * This manipulate the style class making it happy, sad or removing either happy or sad for neutral.
 */
trait WithDisposition extends Styleable {
  neutral()

  def disposition(boolean: Boolean): Unit = {
    if (boolean)
      happy()
    else
      sad()
  }

  def happy(): Unit = disposition(Disposition.happy)

  def sad(): Unit = disposition(Disposition.sad)

  def neutral(): Unit = disposition(Disposition.neutral)

  def disposition(happySadNeutral: Disposition): Unit = {
    styleClass.remove("sad")
    styleClass.remove("happy")
    if (happySadNeutral != Disposition.neutral)
      styleClass.addOne(happySadNeutral.getStyle)
  }
}
