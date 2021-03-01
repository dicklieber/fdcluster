package org.wa9nnn.fdcluster.adif

import org.specs2.mutable.Specification

import scala.io.Source

class AdifCollectorSpec extends Specification {

  "AdifCollectorSpec" should {
    "read" in {
      val url = getClass.getResource("/ARRL-FIELD-DAY.adi")
      val source = Source.fromURL(url)
      val adifFile = AdifCollector.read(source)
      adifFile.header must haveSize(4)
      adifFile.header(2).toString must beEqualTo ("""AdifEntry(
                                                    |,PROGRAMID,N3FJP's ARRL Field Day Contest Log)""".stripMargin)
      adifFile.records must haveSize(54)
      adifFile.records.last.toString must beEqualTo ("""Qso(List(AdifEntry(
                                                       |
                                                       |,Call,N5QJ), AdifEntry(
                                                       |,QSO_Date,20200628), AdifEntry(
                                                       |,Time_On,174800), AdifEntry(
                                                       |,Band,20M), AdifEntry(
                                                       |,Comment,ARRL-FIELD-DAY), AdifEntry(
                                                       |,N3FJP_COMPUTERNAME,WINDOWS-6E8QR35), AdifEntry(
                                                       |,Contest_ID,ARRL-FIELD-DAY), AdifEntry(
                                                       |,Cont,NA), AdifEntry(
                                                       |,Country,USA), AdifEntry(
                                                       |,DXCC,291), AdifEntry(
                                                       |,CQz,04), AdifEntry(
                                                       |,Class,1D), AdifEntry(
                                                       |,N3FJP_Initials,JP), AdifEntry(
                                                       |,ITUz,07), AdifEntry(
                                                       |,Mode,SSB), AdifEntry(
                                                       |,N3FJP_ModeContest,PH), AdifEntry(
                                                       |,OPERATOR,KD9BYW), AdifEntry(
                                                       |,Pfx,N5), AdifEntry(
                                                       |,QSL_Sent,N), AdifEntry(
                                                       |,QSL_Rcvd,N), AdifEntry(
                                                       |,ARRL_Sect,AR), AdifEntry(
                                                       |,N3FJP_SPCNum,AR), AdifEntry(
                                                       |,State,AR), AdifEntry(
                                                       |,N3FJP_StationID,WINDOWS-6E8QR35), AdifEntry(
                                                       |,N3FJP_TransmitterID,0)))""".stripMargin)
    }
  }
}
