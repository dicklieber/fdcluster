package org.wa9nnn.fdcluster.javafx

import javafx.scene.input.KeyCode
import javafx.scene.robot.Robot
import org.specs2.mutable.Specification
import org.wa9nnn.util.ScalafxFixture

class CallSignFieldSpec extends Specification with ScalafxFixture{
  val robot = new Robot()
  "CallSignField" should {
    "happy" in {
      val callSignField = new CallSignField()
      robot.keyType(KeyCode.getKeyCode("1"))
      pending
    }

    "onDone" in {
      ok
    }
  }
}
