
package org.wa9nnn.util

import com.typesafe.config.Config

import java.nio.file.{Files, Paths}

object LogFilePath {
  /**
   * This just set "log.file.path" system property so logback .xml doesn't need explicit file key.
   *
   * @param config with directory set
   */
  def apply(config: Config): Unit = {
    val path = Paths.get(config.getString("log"))
    Files.createDirectories(path.getParent)
    val logFile = path.toAbsolutePath.toString
    System.setProperty("log.file.path", logFile)
  }
}
