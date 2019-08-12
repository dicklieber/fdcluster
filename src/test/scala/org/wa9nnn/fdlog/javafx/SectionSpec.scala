package org.wa9nnn.fdlog.javafx

import org.specs2.mutable.Specification


class SectionSpec extends Specification {

  "SectionSpec" should {
    "ordered data" in {
      val head = Sections.sections.head
      val last = Sections.sections.last
      head.code must beEqualTo("AB")
      last.code must beEqualTo("WY")
    }

    "find" in {
      def justSections(in: Seq[Section]): String = {
        in.map(_.code).mkString(", ")
      }

      justSections(Sections.find("S")) must beEqualTo("SB, SC, SCV, SD, SDG, SF, SFL, SJV, SK, SNJ, STX, SV")
      justSections(Sections.find("N")) must beEqualTo("NC, ND, NE, NFL, NH, NL, NLI, NM, NNJ, NNY, NT, NTX, NV")
      justSections(Sections.find("NN")) must beEqualTo("NNJ, NNY")
    }

  }
}
