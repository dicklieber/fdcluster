
/*
 * Copyright (C) 2017  Dick Lieber, WA9NNN
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.wa9nnn.fdcluster.model

import org.wa9nnn.fdcluster.javafx.Section
import org.wa9nnn.fdcluster.javafx.entry.{EntryCategory, Sections}
import org.wa9nnn.fdcluster.model.Exchange.classParser
import play.api.libs.json._

import scala.util.matching.Regex

class Exchange(val entryClass: String = EntryCategory.defaultCategory.category, val section: String = Sections.sortedByCode.head.code) {
  def display: String = s"$entryClass $section"


  //todo init with contest legal stuff
  def transmitters: Int = {
    try {
      val classParser(nTtransmitters, _) = entryClass
      nTtransmitters.toInt
    } catch {
      case _: Exception =>
        1
    }
  }

  /**
   *
   * @return compact form
   */
  override def toString: String = s"""$entryClass;$section"""

  def canEqual(other: Any): Boolean = other.isInstanceOf[Exchange]

  override def equals(other: Any): Boolean = other match {
    case that: Exchange ⇒
      (that canEqual this) &&
        entryClass == that.entryClass &&
        section == that.section
    case _ ⇒ false
  }

  override def hashCode(): Int = {
    Seq(entryClass, section).map(_.hashCode()).foldLeft(0)((a, b) ⇒ 31 * a + b)
  }
}

object Exchange {
  val classParser: Regex = """(\d+)([HIO])""".r

  def apply(category: String, section: String): Exchange = {
    new Exchange(category.toUpperCase, section.toUpperCase)
  }

  def apply(transmitters:Int, category: EntryCategory, section:Section):Exchange = {
    new Exchange()
  }

  def apply(in: String): Exchange = {
    in match {
      case Parse(category, section) ⇒
        Exchange(category, section)
      case _ ⇒
        throw new IllegalArgumentException(s"Can't parse exchange: $in")
    }
  }

  private val Parse = """(\d*\p{Upper});(.*)""".r
  /**
   * to make JSON a bit more compact
   */
  implicit val sessionKeyFormat: Format[Exchange] = new Format[Exchange] {
    override def reads(json: JsValue): JsResult[Exchange] = {
      val ss = json.as[String]
      try {
        ss match {
          case Parse(category, section) ⇒
            JsSuccess(Exchange(category, section))
          case _ ⇒
            JsError(s"Exchange: $ss could not be parsed!")
        }
      }
      catch {
        case e: IllegalArgumentException ⇒ JsError(e.getMessage)
      }
    }

    override def writes(exchange: Exchange): JsValue = {
      JsString(exchange.toString)
    }
  }
}

