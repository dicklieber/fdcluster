
package org.wa9nnn.fdcluster.adif

trait Field extends Product{
  val name:String
  val dataTypeIndicator:String
}

case class AString(name:String, value:String) extends Field{
  override val dataTypeIndicator: String = "S"
}
