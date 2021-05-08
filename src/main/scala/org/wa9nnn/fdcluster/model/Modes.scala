package org.wa9nnn.fdcluster.model

import com.typesafe.config.{Config, ConfigValue}
import org.wa9nnn.fdcluster.model.CurrentStation.Mode

import java.util
import scala.jdk.CollectionConverters.CollectionHasAsScala

class Modes (contestConfig:Config){
  val rigModeToContestMode: Predef.Map[Mode, Mode] = {
    val config1 = contestConfig.getConfig("modes")
    (for {
      mc: util.Map.Entry[Mode, ConfigValue] <- config1.entrySet.asScala
      contestMode = mc.getKey
      rigMode <- mc.getValue.unwrapped().toString.split("""\s+""")
    } yield {
      rigMode -> contestMode
    }).toMap
  }
  val defaultMode: Mode = rigModeToContestMode.getOrElse("*", "DI")

  val modes: List[Mode] = rigModeToContestMode.values.toSet
    .toList.sorted

  def modeForRig(rig: String): String = {
    rigModeToContestMode.getOrElse(rig, defaultMode)
  }

}
