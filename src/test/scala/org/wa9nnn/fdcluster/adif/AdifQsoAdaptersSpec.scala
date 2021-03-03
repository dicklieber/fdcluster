package org.wa9nnn.fdcluster.adif

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.Qso

import scala.io.Source

class AdifQsoAdaptersSpec extends Specification {

  private val sAdif =
    """ADIF Export from N3FJP's ARRL Field Day Contest Log 6.3
      |Written by G. Scott Davis
      |www.n3fjp.com
      |Log exported on: 3/1/2021 10:28:33 AM
      |<LOG_PGM:34>N3FJP's ARRL Field Day Contest Log
      |<LOG_VER:3>6.3
      |<PROGRAMID:34>N3FJP's ARRL Field Day Contest Log
      |<PROGRAMVERSION:3>6.3
      |<EOH>
      |
      |<Call:5>K0USA
      |<QSO_Date:8>20200627
      |<Time_On:6>211600
      |<Band:3>20M
      |<Comment:14>ARRL-FIELD-DAY
      |<N3FJP_COMPUTERNAME:15>WINDOWS-6E8QR35
      |<Contest_ID:14>ARRL-FIELD-DAY
      |<Cont:2>NA
      |<Country:3>USA
      |<DXCC:3>291
      |<CQz:2>04
      |<Class:2>2E
      |<N3FJP_Initials:2>JP
      |<ITUz:2>07
      |<Mode:3>SSB
      |<N3FJP_ModeContest:2>PH
      |<OPERATOR:6>KD9BYW
      |<Pfx:2>K0
      |<QSL_Sent:1>N
      |<QSL_Rcvd:1>N
      |<ARRL_Sect:3>ENY
      |<N3FJP_SPCNum:3>ENY
      |<State:2>NY
      |<N3FJP_StationID:15>WINDOWS-6E8QR35
      |<N3FJP_TransmitterID:1>0
      |<eor>
      |""".stripMargin

  val adifFile: AdifFile = AdifCollector.read(Source.fromString(sAdif))
  private val adifQso: AdifQso = adifFile.records.head

  "AdifQsoAdaptersSpec" should {
    "happy" in {
      val qso: Qso = AdifQsoAdapter(adifQso)
      qso.callsign must beEqualTo("K0USA")
    }
    "no ARRL_Sect" in {
      val toRemove = AdifEntry("ARRL_SECT", "ENY")
      val missingSection = adifQso.copy(entries = adifQso.entries.filterNot(e => e.tag == "ARRL_SECT"))
      AdifQsoAdapter(missingSection) must throwAn(new MissingRequiredTag("ARRL_SECT"))
    }

    "model to adif" >> {
      val model: Qso = AdifQsoAdapter(adifQso)
      val backAgain = AdifQsoAdapter(model)
      adifQso.contains(backAgain)
    }
  }
}
