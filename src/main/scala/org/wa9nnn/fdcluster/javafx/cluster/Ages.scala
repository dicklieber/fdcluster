package org.wa9nnn.fdcluster.javafx.cluster

import com.typesafe.config.Config

import java.time.{Duration, Instant}

/**
 * The logic of determining if a bith it too old or dead.
 * Note dea will also show old.
 *
 * @param oldAge   more than this is old
 * @param deathAge more than this is dead.
 */
case class Ages(oldAge: Duration = Duration.ofMinutes(1), deathAge: Duration = Duration.ofMinutes(2)) {
  private val oldSeconds: Long = oldAge.toSeconds
  private val deathSeconds: Long = deathAge.toSeconds

  def ageInSeconds(birth: Instant, now: Instant): Long = Duration.between(birth, now).toSeconds

  def old(ageInSeconds:Long): Boolean = {
    ageInSeconds > oldSeconds
  }

  def death(ageInSeconds:Long): Boolean = {
    ageInSeconds > deathSeconds
  }
}

object Ages {
  def apply(config: Config): Ages = {
    new Ages(
      config.getDuration("fdcluster.cluster.nodeConsideredOld"),
      config.getDuration("fdcluster.cluster.nodeConsideredDead"))
  }
}