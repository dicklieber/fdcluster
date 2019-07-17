
package org.wa9nnn.fdlog.javafx

import javafx.collections.ObservableList
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Label, TextArea, TextField}
import scalafx.scene.layout.{BorderPane, HBox, VBox}

class FDLogEntryScene {

  val qsoCallsign: TextField = new TextField()
  val qsoClass: TextField = new TextField()
  val qsoSection: TextField = new TextField()
  var sectionPrompt = new TextArea()
  private val stylesheets: ObservableList[String] = sectionPrompt.getStylesheets

  val scene: Scene = new Scene {
    root = new BorderPane {
      padding = Insets(25)
      center = new HBox(
        new VBox(
          new Label("Callsign"),
          qsoCallsign
        ),
        new VBox(
          new Label("Class"),
          qsoClass
        ),
        new VBox(
          new Label("Section"),
          qsoSection,
          sectionPrompt
        )
      )
    }
  }

}
