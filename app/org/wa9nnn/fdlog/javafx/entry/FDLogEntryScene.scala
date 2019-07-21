
package org.wa9nnn.fdlog.javafx

import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, TextArea, TextField}
import scalafx.scene.layout.{BorderPane, HBox, VBox}

/**
  * Create JavaFX UI for field day entry mode.
  */
class FDLogEntryScene {

  val qsoCallsign: TextField = new TextField() {
    styleClass.append("sadQso")
  }
  val qsoClass: TextField = new TextField() {
    styleClass.append("sadQso")
  }
  val qsoSection: TextField = new TextField()
  qsoSection.getStyleClass.add("sadQso")

  var sectionPrompt = new TextArea()

  val qsoSubmit = new Button("Log")
  qsoSubmit.disable = true
  qsoSubmit.getStyleClass.add("sadQso")

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
          qsoClass,
          qsoSubmit
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
