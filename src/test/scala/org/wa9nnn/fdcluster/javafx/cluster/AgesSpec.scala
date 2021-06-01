package org.wa9nnn.fdcluster.javafx.cluster

import org.specs2.mutable.Specification

import java.time.Instant

class AgesSpec extends Specification {
  val ages: Ages = Ages()

  "AgesSpec" should {
    "alive" in {
      ages.old(20) must beFalse
      ages.death(20) must beFalse
    }
    "old" in {
      ages.old(65) must beTrue
      ages.death(65) must beFalse
    }
    "dead" in {
      ages.old(124) must beTrue
      ages.death( 124) must beTrue
    }

  }
}
