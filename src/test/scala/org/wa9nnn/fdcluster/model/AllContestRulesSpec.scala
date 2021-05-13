package org.wa9nnn.fdcluster.model

import com.typesafe.config.{Config, ConfigFactory}
import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.MockFileContext
import org.wa9nnn.fdcluster.contest.Contest
import org.wa9nnn.fdcluster.tools.MockPersistence
import org.wa9nnn.util.ScalafxFixture

class AllContestRulesSpec extends Specification with ScalafxFixture{

  private val config: Config = ConfigFactory.load()
  private val contestProperty = new ContestProperty( MockFileContext())
  "AllContestRulesSpec" should {
    "names" in {
       val allContestRules = new AllContestRules(config, contestProperty)
      val contestNames = allContestRules.contestNames
      contestNames must contain("FieldDay")
      contestNames must contain("WinterFieldDay")
      contestNames must haveLength(2)
    }

    "default" >> {
       val allContestRules = new AllContestRules(config, contestProperty)
      val rules = allContestRules.currentRules
      rules.contestName must beEqualTo ("FieldDay")

      val contest: Contest = contestProperty.value

      val newContest = contest.copy(contestName = "WinterFieldDay")
      contestProperty.value = newContest
      val switched = allContestRules.currentRules
      switched.contestName must beEqualTo ("WinterFieldDay")
    }


  }
}
