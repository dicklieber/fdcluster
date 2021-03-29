
package org.wa9nnn.fdcluster.contest

import com.typesafe.config.{Config, ConfigList, ConfigValue}
import configs.syntax._
import configs.{ConfigReader, Result}

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters._

case class ScoringRule(name: String, description: String,
                       pointsPer: Int,
                       per: Option[String] = None,
                       maxPoints: Option[Int] = None,
                       categories: Option[String] = None) {


}

class ScoringRules(config: Config) {

  private val configList: ConfigList = config.getList("contest.rules")
  private val scala: mutable.Buffer[AnyRef] = configList.unwrapped().asScala
  println(scala)

  val someduration: Result[FiniteDuration] = ConfigReader[FiniteDuration].read(config, "someduration")


  val d: Result[String] = ConfigReader[String].read(config, "directory")
  private val configRules: mutable.Buffer[ConfigValue] = config.getList("contest.rules").asScala

  configRules.map { c =>
    val sr = c.extract[ScoringRule]
    sr

  }


  val result: Result[ScoringRule] = config.get[ScoringRule]("contest.100%")
  println(result)
}

