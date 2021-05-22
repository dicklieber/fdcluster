
package org.wa9nnn.fdcluster

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.FieldCount.Sorter
import org.wa9nnn.fdcluster.javafx.entry.Sections
import org.wa9nnn.fdcluster.model.Qso
import org.wa9nnn.fdcluster.store.AddQsoListener

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap
import scala.collection.mutable

/**
 * Keeps counts for many Qso fields.
 *
 * @param allQsos the data
 */
@Singleton
class QsoCountCollector @Inject()() extends AddQsoListener with LazyLogging {
  override def add(Qso: Qso): Unit = {
    collectors.foreach(_.ingest(Qso))
  }

  var collectors: Seq[StatCollector] = _

  //@formatter:off
  def clear():Unit =
  collectors= Seq(
    StatCollector("Section", "ARRL Section Worked"){_.exchange.sectionCode},
    StatCollector("Area","US callSign area, CA for Canada or DX for other places."){ q => Sections.callAreaForSection(q.exchange.sectionCode)},
    StatCollector("Band","Worked"){_.bandMode.bandName},
    StatCollector("Mode","Contest Mode"){_.bandMode.modeName},
    StatCollector("Operator", "Operator's CallSign. As set on the Entry Tab."){_.qsoMetadata.operator},
    StatCollector("Rig", "Rig description. As set on the Entry Tab."){_.qsoMetadata.rig},
    StatCollector("Antenna","Antenna. As set on the Entry Tab."){_.qsoMetadata.ant},
    StatCollector("Node", "In Cluster"){_.qsoMetadata.node},
  )
  //@formatter:on

  clear()

  def dumpStats(): Unit = {
    collectors.foreach {
      _.dump()
    }
  }

}

case class StatCollector(name: String, tooltip: String)(fieldExtractor: Qso => String) extends Ordered[StatCollector] {

  private val countsPerThing: mutable.Map[String, AtomicInteger] = TrieMap[String, AtomicInteger]()

  def ingest(Qso: Qso): Unit = {
    val thing = fieldExtractor(Qso)
    countsPerThing.getOrElseUpdate(thing, new AtomicInteger()).incrementAndGet()
  }


  def data(sorter: Sorter): Seq[FieldCount] =
    sorter(countsPerThing
      .map(t2 => FieldCount(t2))
      .toSeq)

  /**
   * Just for testing until UI in place
   */
  def dump(): Unit = {
    println(name + ":")
    countsPerThing
      .iterator
      .map { case (s, ai) => s -> ai.get() }
      .toSeq
      .sortBy(_._1)
      .foreach { case (label, count) =>
        println(f"\t$label: $count%,4d")
      }

  }

  override def compare(that: StatCollector): Int = this.name.compareToIgnoreCase(that.name)
}

case class FieldCount(field: String, count: Int) {

}

object FieldCount {
  type Sorter = Seq[FieldCount] => Seq[FieldCount]

  val byFieldx: Sorter = (f: Seq[FieldCount]) => {
    f.sortBy(_.field)
  }
  val byCountx: Sorter = (f: Seq[FieldCount]) => {
    f.sortBy(_.count)
      .reverse
  }

  val byField: FieldCountOrder = FieldCountOrder("By Field", byFieldx)
  val byCount: FieldCountOrder = FieldCountOrder("By Count", byCountx)

  //  val s: Seq[Seq[FieldCount] => Seq[FieldCount]] = Seq(byFieldx, byCount)

  def apply(t2: (String, AtomicInteger)): FieldCount = {
    new FieldCount(t2._1, t2._2.get())
  }
}

case class FieldCountOrder(name: String, sorter: Sorter)

