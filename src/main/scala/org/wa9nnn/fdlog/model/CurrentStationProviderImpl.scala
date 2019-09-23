
package org.wa9nnn.fdlog.model

import java.nio.file.{Files, NoSuchFileException, Paths}

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import org.wa9nnn.fdlog.model.MessageFormats._
import play.api.libs.json.Json
import resource._

case class CurrentStation(ourStation: OurStation = OurStation("WA9NNN", "IC-7300", "endfed"),
                          bandMode: BandMode = new BandMode("20m", "digital")) {
}

trait CurrentStationProvider {
  /**
   * Currently configured
   *
   * @return
   */
  def currentStation: CurrentStation

  def bandMode: BandMode = currentStation.bandMode

  def ourStation: OurStation = currentStation.ourStation

  def update(currentStation: CurrentStation): Unit
}

/**
 * Manages CurrentStation
 * Loads from file as specified in fdlog.currentStationPath on startup.
 * Saves on update.
 *
 * @param config configuration.
 */
class CurrentStationProviderImpl @Inject()(config: Config = ConfigFactory.load())
  extends CurrentStationProvider with LazyLogging {

  private val currentStationPath = Paths.get(config.getString("fdlog.currentStationPath"))
  Files.createDirectories(currentStationPath.getParent)

  /**
   * Currently configured
   *
   * @return
   */
  private var currentStation_ = {
    try {
      managed(Files.newInputStream(currentStationPath)) acquireAndGet {
        inputStream =>
          val cs = Json.parse(inputStream).as[CurrentStation]
          logger.info(s"Loaded CurrentStation from $currentStationPath")
          cs
      }
    } catch {
      case nsf: NoSuchFileException ⇒
        logger.info(s"No $currentStationPath using default.")
        CurrentStation()
      case et: Throwable ⇒
        et.printStackTrace()
        CurrentStation()
    }
  }
  // tt.getOrElse(CurrentStation())

  override def update(currentStation: CurrentStation): Unit = {
    currentStation_ = currentStation
    managed(Files.newOutputStream(currentStationPath)) acquireAndGet {
      outputStream ⇒
        outputStream.write(Json.prettyPrint(Json.toJson(currentStation)).getBytes)
        logger.info(s"Save updated CurrentStation to $currentStationPath")
    }

  }

  /**
   * Currently configured
   *
   * @return
   */
  override def currentStation: CurrentStation = currentStation_
}


case class OurStation(operator: CallSign, rig: String = "", antenna: String = "")


