
package org.wa9nnn.fdcluster

import org.apache.commons.io.FileUtils

import java.nio.file.{Files, Path}
import scala.language.implicitConversions

case class MockFileContext() {
  val mockUserdir: Path = Files.createTempDirectory("mockUserDir")
  System.setProperty("user.home", mockUserdir.toAbsolutePath.toString)


  val fileContext: FileContext = new FileContext()


  def clean(): Unit = {
    FileUtils.deleteDirectory(fileContext.directory.toFile)
  }

}

object MockFileContext {
  implicit def mock2Fm(b: MockFileContext): FileContext = b.fileContext
}
