package org.wa9nnn.fdlog.javafx

import org.specs2.mutable.Specification


class SectionSpec extends Specification {

  "SectionSpec" should {
    val sections = new Sections()
    "ordered data" in {
      val head = sections.sections.head
      val last = sections.sections.last
      head.section must beEqualTo("AB")
      last.section must beEqualTo("WY")
    }

    "find" in {
      def justSections(in: Seq[Section]): String = {
        in.map(_.section).mkString(", ")
      }

      justSections(sections.find("S")) must beEqualTo("SB, SC, SCV, SD, SDG, SF, SFL, SJV, SK, SNJ, STX, SV")
      justSections(sections.find("N")) must beEqualTo("NC, ND, NE, NFL, NH, NL, NLI, NM, NNJ, NNY, NT, NTX, NV")
      justSections(sections.find("NN")) must beEqualTo("NNJ, NNY")
    }

  }
}
