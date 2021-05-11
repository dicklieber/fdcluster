
package org.wa9nnn.fdcluster

import org.apache.commons.io.FileUtils

import java.nio.file.{Files, Path}
import scala.language.implicitConversions

case class MockFileManager()  {
   val mockUserdir: Path = Files.createTempDirectory("mockUserDir")
  System.setProperty("user.home", mockUserdir.toAbsolutePath.toString)


  val fileManager: FileManager = new FileManager()


  def clean(): Unit = {
    FileUtils.deleteDirectory(fileManager.directory.toFile)
  }

}

object MockFileManager {
  implicit def mock2Fm(b: MockFileManager):FileManager = b.fileManager
}
