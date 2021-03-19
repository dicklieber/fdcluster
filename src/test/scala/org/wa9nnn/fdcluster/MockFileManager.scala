
package org.wa9nnn.fdcluster

import org.apache.commons.io.FileUtils
import org.wa9nnn.fdcluster.model.{Contest, ContestProperty}
import scalafx.beans.property.ObjectProperty

import java.nio.file.{Files, Path}

case class MockFileManager() extends FileManager {
  override def directory: Path = Files.createTempDirectory("mockfileManager")

  def clean(): Unit = {
    FileUtils.deleteDirectory(directory.toFile)
  }

}
