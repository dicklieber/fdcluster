package org.wa9nnn.fdcluster.rig

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.javafx.StatusPane
import org.wa9nnn.fdcluster.model.Station.Band
import org.wa9nnn.fdcluster.model.{AllContestRules, StationProperty}
import scalafx.beans.property.StringProperty

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

@Singleton
class RigInfo @Inject()(rigStore: RigStore, actorSystem: ActorSystem,
                        currentStationProperty: StationProperty,
                        allContestRules: AllContestRules,
                        statusPane: StatusPane
                       ) extends Runnable with LazyLogging {
  def connectToRigctld(): Option[RigIo] = {

    val rigSettings = rigStore.rigSettings.value
    if (rigSettings.enable) {
      val option: Option[RigIo] = Try {
        new RigIo(SocketAdapter(rigSettings.rigctldHostPort, 4532))
      }.toOption
      mayBeRigIo = option
      option
    } else
      None
  }

  var mayBeRigIo: Option[RigIo] = connectToRigctld()

  val duration: FiniteDuration = (2 seconds)
  actorSystem.getScheduler.scheduleWithFixedDelay(duration, duration)(this)

  val rigState: StringProperty = new StringProperty("-")

  override def run(): Unit = {
    try {
      mayBeRigIo
        .orElse(connectToRigctld())
        .foreach { rigIo =>
          val frequencyInt = rigIo.frequency
          val frequency = frequencyInt.toDouble
          val mhz = frequency / 1000000.0
          val (mode, _) = rigIo.modeAndBandWidth
          val maybeBand = allContestRules.currentRules.bands.band(frequencyInt)
          onFX {
            val sMhz = f"$mhz%.3fMHz"
            rigState.value = f"$sMhz $mode"
            maybeBand match {
              case Some(value: Band) =>
                currentStationProperty.bandNameProperty.value = value
                statusPane.clear()
              case None =>
                statusPane.messageSad(s"$sMhz not in contest band!")
            }
            currentStationProperty.modeNameProperty.value = allContestRules.currentRules.modes.modeForRig(mode)
          }
        }
    } catch {
      case e: Exception =>
        logger.warn("RigInfo run", e)
    }
  }
}
