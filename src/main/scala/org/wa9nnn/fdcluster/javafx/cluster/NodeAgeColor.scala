package org.wa9nnn.fdcluster.javafx.cluster

import com.wa9nnn.util.AgeColor
import scalafx.css.Styleable

import java.time.Instant

object NodeAgeColor {
  /**
   * Baed in the current age of the [[Instant]], set the css style class of some Styleable.
   *
   * @param instant   since.
   * @param styleable to apply style to.
   */
  def apply(instant: Instant, styleable: Styleable): Unit = {
    styleable.styleClass.removeAll(styles: _*)
    styleable.styleClass += ageColor(instant)
  }


  private val prefix: String = "age"
  /**
   * Mapping seconds old to css classes.
   */
  val defaultMapping: Seq[(Int, String)] = Seq[(Int, String)](
    7 -> s"${prefix}Current",
    10 -> s"${prefix}NotCurrent",
    30 -> s"${prefix}Old",
    60 -> s"${prefix}Older",
    0x7fffffff -> s"${prefix}Max")

  val ageColor = new AgeColor(defaultMapping)

  val styles: Seq[String] = defaultMapping.map(_._2)

}

