package org.wa9nnn.util

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.javafx.ValueName

class ClassNameSpec extends Specification {

  "ClassName" should {
    "last" in {
      ClassName.last(classOf[ValueName]) must beEqualTo ("ValueName")
    }
  }
}
