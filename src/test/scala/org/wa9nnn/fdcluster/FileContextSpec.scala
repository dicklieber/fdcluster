package org.wa9nnn.fdcluster

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.{ContestProperty, ExportFile}

import java.nio.file.{Path, Paths}

class FileContextSpec extends Specification  with Mockito {
  System.setProperty("instance", "42")
  val userHome: Path = Paths.get(System.getProperty("user.home"))
  val fileManager = new FileContext

  "values" >> {
    fileManager.userDir must beEqualTo (userHome)
    val directory = fileManager.directory
    fileManager.instance must beEqualTo (42)
    fileManager.logFile must endWith("/fdcluster42/logs/fdcluster.log")
    fileManager.journalDir must beEqualTo (directory.resolve("journal"))
    fileManager.varDirectory must beEqualTo (directory.resolve("var"))
  }
  "defaultExportFile" >> {
    val contestProperty = mock[ContestProperty]
    contestProperty.contestName returns("NFD")
    val exportFile: ExportFile = fileManager.defaultExportFile(".nnn", contestProperty)
    exportFile.fileName must beEqualTo ("NFD-2021.nnn")
    exportFile.directory must endWith("/NFD")
  }

  "mock" >> {
    val mockFileManager = new MockFileContext
    import org.wa9nnn.fdcluster.MockFileContext.mock2Fm
    val fm:FileContext = mockFileManager
    fm.userDir must beEqualTo (mockFileManager.mockUserdir)
  }
}
