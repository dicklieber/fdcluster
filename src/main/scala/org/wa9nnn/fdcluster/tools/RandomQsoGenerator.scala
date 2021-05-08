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

import org.wa9nnn.fdcluster.javafx.entry.{NullRunningTaskConsumer, RunningTaskInfoConsumer}
import org.wa9nnn.fdcluster.javafx.runningtask.RunningTask
import org.wa9nnn.fdcluster.model.{AllContestRules, EntryCategories, Qso}

import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class RandomQsoGenerator @Inject()(allContestRules: AllContestRules, val runningTaskInfoConsumer: RunningTaskInfoConsumer = new NullRunningTaskConsumer()) {

 val entryCategories: EntryCategories =  allContestRules.currentRules.categories

  def apply(gr: GenerateRandomQsos): (Qso => Unit) => Unit = {
    new Task(runningTaskInfoConsumer, entryCategories)(gr)
  }

  class Task(val runningTaskInfoConsumer: RunningTaskInfoConsumer, entryCategories: EntryCategories) extends RunningTask {
    override def taskName: String = "Generate Random QSOs"

    var bandMode = new RandomBandMode()
    val randomExchange = new RandomExchange(entryCategories)
    val callSign = new RandomCallsign


    def apply(gr: GenerateRandomQsos)(f: Qso => Unit): Unit = {
      totalIterations = gr.ntoGen
      var lastStamp = Instant.now().minus(gr.hoursBefore, ChronoUnit.HOURS)

      for (_ <- 0 until  gr.ntoGen) {
        f(Qso(callSign.next, bandMode.next, randomExchange.next(), lastStamp))
        countOne()
        lastStamp = lastStamp.plus(gr.between)
      }
      done()
    }
  }
}

/**
 *
 * @param ntoGen      how many.
 * @param hoursBefore where to start
 * @param between     how much to advance the 'clock' between each QSO.
 */
case class GenerateRandomQsos(ntoGen: Int = 10000,
                              hoursBefore: Int = 23,
                              between: java.time.Duration = java.time.Duration.ofSeconds(5))
