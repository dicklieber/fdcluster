package org.wa9nnn.fdcluster

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.{ContestProperty, ExportFile}

import java.nio.file.{Path, Paths}

class FileManagerSpec extends Specification  with Mockito {
  System.setProperty("instance", "42")
  val userHome: Path = Paths.get(System.getProperty("user.home"))
  val fileManager = new FileManager

  "values" >> {
    fileManager.userDir must beEqualTo (userHome)
    val directory = fileManager.directory
    fileManager.instance must beEqualTo (42)
    fileManager.logFile must endWith("/fdcluster42/logs/fdcluster.log")
    fileManager.journalDir must beEqualTo (directory.resolve("journal"))
    fileManager.varDirectory must beEqualTo (directory.resolve("var"))
    fileManager.httpPort must beEqualTo (8080 + 42)
  }
  "defaultExportFile" >> {
    val contestProperty = mock[ContestProperty]
    contestProperty.contestName returns("NFD")
    val exportFile: ExportFile = fileManager.defaultExportFile(".nnn", contestProperty)
    exportFile.fileName must beEqualTo ("NFD-2021.nnn")
    exportFile.directory must endWith("/NFD")
  }

  "mock" >> {
    val mockFileManager = new MockFileManager
    import org.wa9nnn.fdcluster.MockFileManager.mock2Fm
    val fm:FileManager = mockFileManager
    fm.userDir must beEqualTo (mockFileManager.mockUserdir)
  }
}
