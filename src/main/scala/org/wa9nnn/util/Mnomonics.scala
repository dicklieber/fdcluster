
package org.wa9nnn.util

object Mnomonics {
  val map: Map[Char, String] = Seq(
    ' ' -> "",
    'A' -> "Alpha ",
    'B' -> "Bravo ",
    'C' -> "Charlie ",
    'D' -> "Delta ",
    'E' -> "Echo ",
    'F' -> "Foxtrot ",
    'G' -> "Golf ",
    'H' -> "Hotel ",
    'I' -> "India ",
    'J' -> "Juliet ",
    'K' -> "Kilo ",
    'L' -> "Lima ",
    'M' -> "Mike ",
    'N' -> "November ",
    'O' -> "Oscar ",
    'P' -> "Papa ",
    'Q' -> "Quebec ",
    'R' -> "Romeo ",
    'S' -> "Sierra ",
    'T' -> "Tango ",
    'U' -> "Uniform ",
    'V' -> "Victor ",
    'W' -> "Whiskey ",
    'X' -> "X-Ray ",
    'Y' -> "Yankee ",
    'Z' -> "Zulu "
  ).toMap

  def apply(callSign: String): String = {
    callSign.toUpperCase.flatMap { x =>
      if (x.isDigit)
        x +: " "
      else
        map.getOrElse(x,"")
    }.trim

  }

}
