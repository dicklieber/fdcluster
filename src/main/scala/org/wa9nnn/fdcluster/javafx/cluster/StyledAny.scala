
/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.fdcluster.javafx.cluster

import java.time.LocalDateTime

import org.wa9nnn.fdcluster.model.TimeFormat.formatLocalDateTime
import org.wa9nnn.util.CssClassProvider
import scalafx.scene.control.Labeled

import scala.collection.JavaConverters._
/**
 *
 * @param value
 * @param cssClasses
 * @param toolTip
 */
case class StyledAny(value: Any, val cssClasses: Seq[String], toolTip:Option[String]) extends CssClassProvider {

  override def toString: String = {
    //todo match to specific values maybe
    value.toString
  }

  def withCssClass(cssClass: String): StyledAny = {
    copy(cssClasses = this.cssClasses :+ cssClass)
  }
  def withCssClass(in: Seq[String]): StyledAny = {
    copy(cssClasses = this.cssClasses ++ in )
  }

  def withToolTip(toolTip: String): StyledAny = {
    copy(toolTip = Some(toolTip))
  }
  def setLabel(cell:Labeled): Unit = {
    cell.styleClass.addAll( cssClasses.asJavaCollection)
    value match {
      case ls:LabelSource ⇒
        ls.setLabel(cell)

      case x ⇒
        x match {
          case ldt:LocalDateTime ⇒
            cell.text = ldt
          case o ⇒
            cell.text = o.toString

        }
    }
  }
}

object StyledAny {
  def apply(value: Any)(implicit ourNode: OurNode = OurNode()): StyledAny = {
    val sa = new StyledAny(value, Seq.empty, None)
    if (ourNode.is) {
      sa.withCssClass("ourNode")
    } else {
      sa
    }
  }
}

case class OurNode(is: Boolean = false)

trait LabelSource {
  def toolTip:String = {""}
  def setLabel(labeled: Labeled)
}

