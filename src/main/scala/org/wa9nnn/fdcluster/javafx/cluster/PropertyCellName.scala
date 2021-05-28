package org.wa9nnn.fdcluster.javafx.cluster

import scalafx.scene.control.Tooltip

trait PropertyCellName {
 def toolTip: String

 def name:String
}

object PropertyCellName {
 val noName: StringCellName = StringCellName("")
}

/**
 * //todo Probably need a [[PropertyCell]] that nas no name
 *
 * @param name
 * @param toolTip
 */
case class StringCellName(name:String, toolTip:String = "") extends PropertyCellName
