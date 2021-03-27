package org.wa9nnn.fdcluster.javafx

import com.typesafe.config.{Config, ConfigFactory}
import javafx.scene.input.KeyCode
import javafx.scene.robot.Robot
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.{AllContestRules, ContestProperty, EntryCategories}
import org.wa9nnn.util.ScalafxFixture

class CallSignFieldSpec extends Specification with ScalafxFixture with Mockito {
  val robot = new Robot()
  "CallSignField" should {
    "happy" in {
      val config: Config = ConfigFactory.load()
      val allContestRules =mock[AllContestRules]
      val contestProperties = mock[ContestProperty]
      val callSignField = new ClassField(allContestRules, contestProperties)
      robot.keyType(KeyCode.getKeyCode("1"))
      pending
    }

    "onDone" in {
      ok
    }
  }
}
