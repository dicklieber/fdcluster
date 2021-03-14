
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

package org.wa9nnn.fdcluster.rig

import com.typesafe.scalalogging.LazyLogging

import scala.sys.process._

class RigList extends  LazyLogging {
  val map: Map[String, Seq[RigModel]]  = {
    val pl: ProcessLogger = new ProcessLogger {
      override def err(s: => String): Unit = {
        logger.trace(s)
      }

      override def out(s: => String): Unit = {
        throw new NotImplementedError() //todo
      }

      override def buffer[T](f: => T) = {
        throw new NotImplementedError() //todo
      }
    }
    Seq("rigctld", "-l").lazyLines_!(pl)
      .drop(1) // get rid of header
      .map { l =>
        val tokens: Array[String] = l.split("""\s+""")
        RigModel(tokens(1).toInt, tokens(2), tokens(3))
      }
      .groupBy(_.mfg)
  }
   val mfgs: Seq[String] = map.keySet.toSeq.sorted

  def modelsForMfg(mfg: String): Seq[RigModel] = {
    map(mfg)
  }

}


case class RigModel(number: Int, mfg: String, model: String) extends Ordered[RigModel] {
  def choice: String = s"$model ($number)"

  override def toString: String = {
    s"$mfg \t$model \t($number)"
  }

  override def compare(that: RigModel): Int = {

    val ret = this.model.compareToIgnoreCase(that.model)
    if (ret != 0)
      this.mfg.compareToIgnoreCase(that.mfg)
    else
      0
  }
}

object RigModel {
  def apply():RigModel = RigModel(-1, "None", "-")
}



