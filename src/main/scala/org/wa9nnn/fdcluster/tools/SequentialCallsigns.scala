package org.wa9nnn.fdcluster.tools

import org.wa9nnn.fdcluster.model.CallSign

import scala.language.{implicitConversions, postfixOps}

class SequentialCallsigns {


  private val end = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
  private val any = " ABCDEFGHIJKLMNOPQRSTUVWXYZ"
  private val start = "AKNW"
  private val areas = "1234567890"


  val stackedChars: SequentialChar =
    SequentialChar(end,
      SequentialChar(any,
        SequentialChar(any,
          SequentialChar(areas,
            SequentialChar(any,
              SequentialChar(start))))))

  def next(): String = {
   stackedChars.next.replace(" ", "")
  }
}

case class SequentialChar private(chars: String, parent: Option[SequentialChar] = None) {
  val reset = "↺"
  private val values: String = chars + reset
  private val it: Iterator[String] = Iterator.continually(values.map(_.toString).toList).flatten

  def pvalue(): String = {
  val r: String =   parent match {
      case Some(value: SequentialChar) =>
        value.next
      case None =>
        ""
    }
    r
  }

  var parentValue:CallSign = pvalue()

  def next: String = {
    val next1: String = it.next
    val str = if (next1 == reset) {
      parentValue = pvalue()
      it.next //skip past reset
    } else {
      next1
    }
    parentValue + str
  }
}

object SequentialChar {
  implicit def sc2o(sequentialChar: SequentialChar): Option[SequentialChar] = Option(sequentialChar)

}


