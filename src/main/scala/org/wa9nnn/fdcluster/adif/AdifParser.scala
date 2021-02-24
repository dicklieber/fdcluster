
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
          f(AdifEntry(logic.consumePredef, name, v))
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

  def consumePredef: String = predefBuilder.result().replace("\r\n", "\n")

  def consumeTag: String = tagBuilder.result()

}