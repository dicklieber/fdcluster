
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

import org.wa9nnn.fdcluster.javafx.entry.Sections
import org.wa9nnn.fdcluster.javafx.entry.section.Section
import org.wa9nnn.fdcluster.model.{EntryCategories, EntryCategory, Exchange}

import java.security.SecureRandom

class RandomExchange(entryCategories: EntryCategories) {
  private val sections: Seq[Section] = Sections.sections
  val random = new SecureRandom()
  val nSections: Int = sections.length
  val categories: Seq[EntryCategory] = entryCategories.categories.toSeq

  def next(): Exchange = {
    val nTransmitters = random.nextInt(20) + 1
    val category = categories(random.nextInt(categories.length))
    val section = sections(random.nextInt(sections.length))
    Exchange(nTransmitters, category, section)
  }
  sections(random.nextInt(nSections))
}
