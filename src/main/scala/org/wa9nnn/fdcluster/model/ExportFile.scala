
package org.wa9nnn.fdcluster.model

import java.nio.file.{Files, Path, Paths}

//send to [[StoreActor]]
case class AdifExportRequest(exportFile: ExportFile = ExportFile())

case class ExportFile(directory: String = System.getProperty("user.home"), fileName: String = "") {
  def validate: Option[String] = {
    if (fileName.isBlank)
      Some("Filename cannot be empty!")
    else
      None
  }

  def path: Path = {
    Files.createDirectories(directoryPath)
    if (!Files.isWritable(directoryPath)) throw new IllegalStateException(s"Can't create or is not-writable: $directoryPath")
    directoryPath.resolve(fileName)
  }

  def directoryPath: Path = Paths.get(directory).toAbsolutePath

  def absoluteDirectory: String = directoryPath.toString
}


