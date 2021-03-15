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

package org.wa9nnn.fdcluster.tools

import org.specs2.mutable.Specification

class RandomCharSpec extends Specification {

  "RandomChar" should {
    "atoz" in {
      val randomLetter = new RandomChar()
      val builder = Set.newBuilder[Char]
      for(n <- 0 to 1000){
        builder.addOne(randomLetter.nextChar)
      }
      val chars = builder.result().toArray.sorted
      chars must haveSize(27)
      val string = new String(chars)
      randomLetter.values must beEqualTo (string)
    }
    "KWAN" in {
      val randomLetter = new RandomChar("AKNW")
      val builder = Set.newBuilder[Char]
      for(_ <- 0 to 1000){
        builder.addOne(randomLetter.nextChar)
      }
      val chars = builder.result().toArray.sorted
      chars must haveSize(4)
      val string = new String(chars)
      randomLetter.values must beEqualTo (string)
    }
    "numeric" in {
      val randomLetter = new RandomChar("0123456789")
      val builder = Set.newBuilder[Char]
      for(_ <- 0 to 1000){
        builder.addOne(randomLetter.nextChar)
      }
      val chars = builder.result().toArray.sorted
      chars must haveSize(10)
      val string = new String(chars)
      randomLetter.values must beEqualTo (string)
    }

  }
}
