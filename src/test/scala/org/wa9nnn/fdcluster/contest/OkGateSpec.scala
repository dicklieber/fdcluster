package org.wa9nnn.fdcluster.contest

import com.wa9nnn.util.tableui.Cell
import org.specs2.mutable.Specification

class OkGateSpec extends Specification {

  "OkGateSpec" should {
    "initial" in {
      val gate = new OkGate
      gate.getBad must haveSize(0)
    }

    "new Item thats ok" in {
      val gate = new OkGate
      gate.update(OkItem("test", "testThing", "reason"){ () => true})
      gate.getBad must haveSize(0)
    }
    "new Item thats not ok" in {
      val gate = new OkGate
      gate.update(OkItem("test", "testThing", "reason"){ () => true})
      val items = gate.getBad
      items must haveSize(1)
      val item: OkItemPropertyCell = items.head
      item.isOk must beFalse
//      val row = item.tableUiRow()
//      val cells = row.cells
//      cells.head must beEqualTo(new Cell("test"))
//      cells(1) must beEqualTo(new Cell("testThing"))
//      cells(2) must beEqualTo(new Cell("Need to do something 1st").withCssClass("sad"))
    }
    "update to ok" in {
      val gate = new OkGate
      gate.update(OkItem("test", "testThing", "reason"){ () => false})
      gate.getBad must haveSize(1)
      gate.update(OkItem("test", "testThing", "reason"){ () => true})
      gate.getBad must haveSize(0)
      val items = gate.getAll
      items must haveSize(1)
      val item = items.head
      item.isOk must beTrue

//      val row = item.tableUiRow()
//      val cells = row.cells
//      cells.head must beEqualTo(new Cell("test"))
//      cells(1) must beEqualTo(new Cell("testThing"))
//      cells(2) must beEqualTo(new Cell("Ok").withCssClass("happy"))
    }
  }
}
