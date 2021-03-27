
package org.wa9nnn.fdcluster.model

import com.typesafe.config.Config
import scalafx.collections.ObservableBuffer

import scala.jdk.CollectionConverters._
import scala.util.matching.Regex

/**
 *
 * @param contestConfig for a specfic contest.
 */
class EntryCategories(contestConfig: Config) {

  private val ec: List[EntryCategory] = contestConfig.getStringList("categories").asScala.map(EntryCategory.fromConfig).toList

  //  designators = values.map(_.designator)
  val categories: ObservableBuffer[EntryCategory] = ObservableBuffer[EntryCategory](ec).sorted

  var designators: Set[String] = ec.map(_.designator).toSet


  def entryCategoryForDesignator(designator: String): EntryCategory = {
    categories
      .find(_.designator == designator)
      .getOrElse(categories.toSeq.head)
  }
  val defaultCategory: EntryCategory ={
    categories.toSeq.head
  }

  def valid(classDesignator: String): Boolean = {
    designators.contains(classDesignator)
  }
}

case class EntryCategory(designator: String = "A", category: String = "Club") extends Ordered[EntryCategory]{
  assert(designator.length == 1, "EntryCategory:designator must be one char long.")

  def buildClass(transmitters: Int): String = s"$transmitters$designator"

  override def compare(that: EntryCategory): Int = this.designator compareTo that.designator

  override def toString: String = s"$designator: $category"
}

object EntryCategory {
  val parser: Regex = """([A-Z]):\s+(.*)""".r

  def fromConfig(configLine: String): EntryCategory = {
    val parser(ch, desc) = configLine
    new EntryCategory(ch, desc)
  }
}

