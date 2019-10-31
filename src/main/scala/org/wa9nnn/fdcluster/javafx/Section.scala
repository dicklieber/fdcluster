
package org.wa9nnn.fdcluster.javafx

/**
  * One ARRL section
  * @param name user friendly name.
  * @param code actual code
  * @param area callsign area
  */
case class Section(name: String, code: String, area: String) extends Ordered[Section] {
  override def compare(that: Section): Int = this.code compareTo that.code
}
