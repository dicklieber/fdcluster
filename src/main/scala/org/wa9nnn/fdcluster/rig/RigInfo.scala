package org.wa9nnn.fdcluster.rig

import akka.actor.ActorSystem
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.model.CurrentStation.Band
import org.wa9nnn.fdcluster.model.{BandMode, BandModeFactory, CurrentStationProperty}
import org.wa9nnn.fdcluster.rig.RigIo.defaultPort
import scalafx.beans.property.{LongProperty, ObjectProperty, StringProperty}

import scala.concurrent.duration._
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

@Singleton
class RigInfo @Inject()(rigStore: RigStore, actorSystem: ActorSystem, currentStationProperty: CurrentStationProperty,
                        bandModeFactory: BandModeFactory)  extends Runnable {

  private val rigIo = new RigIo(SocketAdapter("127.0.0.1", defaultPort))

  val duration: FiniteDuration = (2 seconds)
  actorSystem.getScheduler.scheduleWithFixedDelay(duration, duration)(this)

  val rigState: StringProperty = new StringProperty("-")
  val bandProperty = new StringProperty()
  val modeProperty = new StringProperty()

  override def run(): Unit = {
    val frequencyInt = rigIo.frequency
    val frequency = frequencyInt.toDouble
    val mhz = frequency / 1000000.0
    val (mode, _) = rigIo.modeAndBandWidth
    val maybeBand = bandModeFactory.band(frequencyInt)
    onFX {
      maybeBand.foreach{ m: Band =>
        bandProperty.value = m
      }

      bandModeFactory.modeForRig(mode).foreach{
        modeProperty.value = _
      }
      rigState.value = f"$mhz%.3fMHz $mode"
    }
  }
}
