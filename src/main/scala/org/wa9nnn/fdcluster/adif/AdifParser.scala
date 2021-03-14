
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

package org.wa9nnn.fdcluster.adif

import org.wa9nnn.fdcluster.adif.AdifParser.tag

import scala.io.Source
import scala.util.matching.Regex

/**
 * Parses an ADIF data
 *
 * @param f invoked with each entry encountered.
 */
class AdifParser(source: Source)(f: AdifResult => Unit) {
  var logic = new Collector

  while (source.hasNext) {
    val ch = source.next()
    ch match {
      case '<' =>
        logic.startTag()
        logic(ch)
      case '>' =>
        logic(ch)
        val sTag = logic.consumeTag
        val tag(name, l, t) = sTag
        if (l == null) {
          f(AdifSeperator(name))
        } else {
          val length = l.toInt
          val v: String = new String(source.take(length).toArray)
            .replace("\r\n", "\n")
          f(AdifEntry(name.toUpperCase, v))
        }
        logic = new Collector

      case c =>
        logic(c)
    }
  }
}

object AdifParser {
  val tag: Regex = """<(\w*)(?::(\d+))?(?::([A-Z]))?>""".r
}

/**
 * Holds data while collecting one ADIF field.
 */
class Collector() {
  private val predefBuilder = new StringBuilder()
  private val tagBuilder = new StringBuilder
  private var currentCollector: StringBuilder = predefBuilder

  def predef(): String = predefBuilder.toString().trim

  def apply(ch: Char): Unit = {
    currentCollector.append(ch)
  }

  def startTag(): Unit = currentCollector = tagBuilder


  def consumeTag: String = tagBuilder.result()

}