package org.wa9nnn.fdcluster.javafx

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.{AllContestRules, ContestProperty}
import org.wa9nnn.util.ScalafxFixture

class ClassFieldSpec extends Specification with Mockito with ScalafxFixture {
  "ClassField" >> {
    val allContestRules = mock[AllContestRules]
    val contestProperty = mock[ContestProperty]
    allContestRules.byContestName("WFD").validDesignator("H") returns (true)
    //    entryCategories.valid("X") returns(false)

    "happy" >> {
      val classField: ClassField = new ClassField(allContestRules, contestProperty)
      classField.text = "1H"
      classField.validProperty.value must beTrue
    }
    "bad category" >> {
      val classField: ClassField = new ClassField(allContestRules, contestProperty)
      classField.text = "1X"
      classField.validProperty.value must beFalse
    }
    "bad transmitters" >> {
      val classField: ClassField = new ClassField(allContestRules, contestProperty)
      classField.text = "-2H"
      classField.validProperty.value must beFalse
    }

  }
}
