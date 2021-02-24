
package org.wa9nnn.fdcluster.adif

/**
 * Collects chars into entry
 * @param f invoked with each entrhy encountered.
 */
class FieldCollector(f: Entry => Unit) {
  var logic = new Logic

  def apply(ch: Char): Unit = {
    ch match {
      case '<' =>
        if (logic.inValue) {
          f(logic.result)
          val newLogic = new Logic()
          newLogic(ch)
          logic = newLogic
        } else {
          logic.startTag()
          logic(ch)
        }
      case '>' =>
        logic(ch)
        logic.startValue()
      case c =>
        logic(c)
    }
  }
}

case class Entry(predef: String, tag: String, value: String){
  override def toString: String = {
    s"$predef$tag$value"
  }
}

class Logic() {
  private val predef = new StringBuilder()
  private val tag = new StringBuilder
  private val value = new StringBuilder
  private var currentCollector: StringBuilder = predef



  def result: Entry = {
    Entry(predef.toString().trim, tag.toString().trim, value.toString().trim)
  }

  def apply(ch: Char): Unit = {
    currentCollector.append(ch)
  }

  def startTag(): Unit = currentCollector = tag

  def startValue(): Unit = currentCollector = value

  def inValue: Boolean = {
    currentCollector eq value
  }
}