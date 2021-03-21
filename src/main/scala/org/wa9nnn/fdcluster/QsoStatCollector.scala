
package org.wa9nnn.fdcluster

import com.google.inject.name.Named
import org.wa9nnn.fdcluster.javafx.entry.Sections
import org.wa9nnn.fdcluster.model.QsoRecord
import scalafx.collections.ObservableBuffer
import scalafx.collections.ObservableBuffer.Change

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap
@Singleton
class QsoStatCollector @Inject()(@Named("allQsos") allQsos: ObservableBuffer[QsoRecord]) {
  allQsos.onChange { (_, changes: Seq[Change[QsoRecord]]) =>
    changes.foreach {
      case ObservableBuffer.Add(position, added) =>
        added.foreach(handle)
      case ObservableBuffer.Remove(position, removed) =>
      case ObservableBuffer.Reorder(start, end, permutation) =>
      case ObservableBuffer.Update(from, to) =>
    }
  }

  val qsoInSection = new TrieMap[String, AtomicInteger]()
  val qsoInBand = new TrieMap[String, AtomicInteger]()
  val qsoInMode = new TrieMap[String, AtomicInteger]()
  val qsoInArea = new TrieMap[String, AtomicInteger]()
  val qsoInOperator = new TrieMap[String, AtomicInteger]()
  val qsoInRig = new TrieMap[String, AtomicInteger]()
  val qsoInAntenna = new TrieMap[String, AtomicInteger]()

  def handle(qsoRecord: QsoRecord): Unit = {
    qsoInSection.getOrElseUpdate(qsoRecord.qso.exchange.section, new AtomicInteger()).incrementAndGet()
    qsoInBand.getOrElseUpdate(qsoRecord.qso.bandMode.bandName, new AtomicInteger()).incrementAndGet()
    qsoInMode.getOrElseUpdate(qsoRecord.qso.bandMode.modeName, new AtomicInteger()).incrementAndGet()
    qsoInArea.getOrElseUpdate(Sections.callAreaForSection(qsoRecord.qso.exchange.section), new AtomicInteger()).incrementAndGet()
    qsoInOperator.getOrElseUpdate(qsoRecord.qsoMetadata.operator, new AtomicInteger()).incrementAndGet()
    qsoInRig.getOrElseUpdate(qsoRecord.qsoMetadata.rig, new AtomicInteger()).incrementAndGet()
    qsoInAntenna.getOrElseUpdate(qsoRecord.qsoMetadata.ant, new AtomicInteger()).incrementAndGet()
  }

  def dumpStats(): Unit = {
    dump("Section", qsoInSection)
    dump("Band", qsoInBand)
    dump("Mode", qsoInMode)
    dump("Area", qsoInArea)
    dump("Operator", qsoInOperator)
    dump("Rig", qsoInRig)
    dump("Antenna", qsoInAntenna)
  }

  def dump(locus: String, data: TrieMap[String, AtomicInteger]): Unit = {
    println(locus + ":")
    data
      .iterator
      .map { case (s, ai) => s -> ai.get() }
      .toSeq
      .sortBy(_._1)
      .foreach { case (label, count) =>
        println(f"\t$label: $count%,4d")
      }

  }
}
