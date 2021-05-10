package org.wa9nnn.fdcluster.rig

import com.typesafe.config.{Config, ConfigFactory}
import org.specs2.execute.StandardResults.{failure, pending}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.wa9nnn.util.Persistence

import java.nio.file.Paths

class RigctldSpec extends Specification with Mockito{
  val config: Config = ConfigFactory.load()

  private val persistence: Persistence = mock[Persistence]
  val rigList = new Rigctld(config.getConfig("fdcluster.rig"), new RigStore(persistence))
  "RigList" should {
    "modelsForMfg" in {
      val icomRigs: Seq[RigModel] = rigList.modelsForMfg("Icom")
      icomRigs.length must be greaterThan (70)
      val maybe705 = icomRigs.find(_.model == "IC-705")
      maybe705 must beSome[RigModel]
      maybe705.get.model must beEqualTo("IC-705")
    }
    "version" in {
      val version = rigList.logVersion
      version.get startsWith ("rigctl Hamlib")
    }

    "mfgs" in {
      val mfgs: Seq[String] = rigList.rigManufacturers
      mfgs.head must beEqualTo("ADAT")
    }


  }
}
