package org.wa9nnn.fdcluster.javafx.cluster



case class NamedValue(name: ValueName, value: Any) extends Ordered[NamedValue] {
  override def compare(that: NamedValue): Int = this.name compareTo(that.name)
}


