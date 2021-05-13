package org.wa9nnn.fdcluster.javafx.debug

import javafx.collections.ObservableList
import javafx.scene.control
import javafx.scene.control.DialogPane
import org.wa9nnn.fdcluster.ClusterControl
import org.wa9nnn.fdcluster.javafx.GridOfControls
import scalafx.beans.property.ObjectProperty
import scalafx.scene.control.{ButtonType, CheckBox, Dialog}
import scalafx.scene.layout.Region

import javax.inject.{Inject, Singleton}

class ResetDialog @Inject()(clusterControl: ClusterControl) extends Dialog {

  private val dp = dialogPane()
  val goc = new GridOfControls()
  private val netUpCheckBox = new CheckBox("up")
  netUpCheckBox.selected = clusterControl.isUp
  netUpCheckBox.selected.onChange { (_, _, checked) =>
    clusterControl.value = checked
  }
  goc.addControl("Network Up", netUpCheckBox)
  dp.setContent(goc)

  private val buttonTypes: ObservableList[control.ButtonType] = dp.getButtonTypes
  buttonTypes.add(ButtonType.Close)

}





































