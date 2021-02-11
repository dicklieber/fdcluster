
package org.wa9nnn.fdcluster.rig

import com.typesafe.scalalogging.LazyLogging

import scala.sys.process._

object RigList extends App with LazyLogging {
  val pl = new ProcessLogger {
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
  val lines = Seq("rigctld", "-l").lazyLines_!(pl)

  val r = lines.drop(1) // get rid of header
    .map { l =>
      val tokens: Array[String] = l.split("""\s+""")
      RigModel(tokens(1).toInt, tokens(2), tokens(3))
    }
  r.sorted.foreach { r => println(r.toString) }
}

case class RigModel(number: Int, mfg: String, model: String) extends Ordered[RigModel] {
  override def toString: String = {
    s"$mfg \t$model \t($number)"
  }

  override def compare(that: RigModel): Int = {

    var ret = this.model.compareToIgnoreCase(that.model)
    if (ret != 0)
      this.mfg.compareToIgnoreCase(that.mfg)
    else
      0
  }
}