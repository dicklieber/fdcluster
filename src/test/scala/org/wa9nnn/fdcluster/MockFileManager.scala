
package org.wa9nnn.fdcluster

import org.apache.commons.io.FileUtils

import java.nio.file.{Files, Path}

case class MockFileManager() extends FileManager("dontcare") {
  override val directory: Path = Files.createTempDirectory("mockfileManager")

  def clean(): Unit = {
    FileUtils.deleteDirectory(directory.toFile)
  }

}
