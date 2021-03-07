package org.wa9nnn.fdcluster.model

import org.specs2.mutable.Specification

import java.nio.file.Paths

class ExportFileSpec extends Specification {

  "ExportFile" should {
    val home = System.getProperty("user.home")
    "default is users home" >> {
      ExportFile().directoryPath.toString must beEqualTo (home)
    }
    "path" in {
      ExportFile().path must beEqualTo (Paths.get(home).resolve(""))
    }
    "path" in {
      ExportFile().absoluteDirectory must beEqualTo (Paths.get(home).toString)
    }

    "realFilename" >> {
      ExportFile().validate must beSome("Filename cannot be empty!")
      ExportFile(fileName = "wa9nnn.adif").validate must  beNone
    }
  }
}
