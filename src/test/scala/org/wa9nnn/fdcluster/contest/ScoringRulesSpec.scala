package org.wa9nnn.fdcluster.contest

import com.typesafe.config.ConfigFactory
import org.specs2.mutable.Specification

class ScoringRulesSpec extends Specification {
  "ScoringRules" >> {
    val rules = new ScoringRules(ConfigFactory.load())
    pending
  }
}
