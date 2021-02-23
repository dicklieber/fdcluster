
package org.wa9nnn.fdcluster.javafx.entry

import javafx.collections.ObservableList
import org.wa9nnn.fdcluster.model.{BandModeFactory, BandModeOperatorStore}
import org.wa9nnn.util.InputHelper.forceCaps
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.{ComboBox, Control, Label}
import scalafx.scene.layout.GridPane

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class BandModeOpPanel @Inject()(bandModeFactory: BandModeFactory,
                                bandModeOperatorStore: BandModeOperatorStore) extends GridPane {
  val rigFreq = new Label()
  val band: ComboBox[String] = new ComboBox[String](bandModeFactory.avalableBands.sorted.map(_.band)) {
    value <==> bandModeOperatorStore.band
  }
  val mode: ComboBox[String] = new ComboBox[String](bandModeFactory.modes.map(_.mode)) {
    value <==> bandModeOperatorStore.mode
  }
  val operator: ComboBox[String] = new ComboBox[String](bandModeOperatorStore.knownOperators) {
    value <==> bandModeOperatorStore.operator
    editable.value = true
  }
  operator.onAction = (event: ActionEvent) => {
    val currentEditText = operator.editor.value.text.value

    println(s"currentEditText: $currentEditText")
    val items: ObservableList[String] = operator.items.value
    if (!items.contains(currentEditText)) {
      items.add(currentEditText)
    }
  }

  val row = new AtomicInteger()

  def add(label: String, control: Control): Unit = {
    val nrow = row.getAndIncrement()
    add(new Label(label + ":"), 0, nrow)
    add(control, 1, nrow)
  }

  add("Rig", rigFreq)
  add("Band", band)
  add("Mode", mode)
  add("Op", operator)
  forceCaps(operator.editor.value)
}


