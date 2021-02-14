
package org.wa9nnn.fdcluster.rig

import scalafx.Includes._
import scalafx.application.{JFXApp, Platform}
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{GridPane, VBox}

object LoginDialogDemo extends JFXApp {

  stage = new JFXApp.PrimaryStage {
//    private val image = new Image("/Users/dlieber/dev/ham/fdcluster/src/main/resources/images/220-Mhz-logo.png")
//    icons += image
    scene = new Scene {
      title = "Custom Dialog Demo"
      content = new VBox {
        children = new Button("Show Login Dialog") {
          onAction = _ => onShowLoginDialog()
        }
        padding = Insets(top = 24, right = 64, bottom = 24, left = 64)
      }
    }
  }

  def onShowLoginDialog(): Unit = {

    case class Result(username: String, password: String)

    // Create the custom dialog.
    val dialog = new Dialog[Result]() {
      initOwner(stage)
      title = "Login Dialog"
      headerText = "Look, a Custom Login Dialog"
//      graphic = new ImageView(this.getClass.getResource("login_icon.png").toString)
    }

    // Set the button types.
    val loginButtonType = new ButtonType("Login", ButtonData.OKDone)
    dialog.dialogPane().buttonTypes = Seq(loginButtonType, ButtonType.Cancel)

    // Create the username and password labels and fields.
    val username = new TextField() {
      promptText = "Username"
    }
    val password = new PasswordField() {
      promptText = "Password"
    }

    val grid = new GridPane() {
      hgap = 10
      vgap = 10
      padding = Insets(20, 100, 10, 10)

      add(new Label("Username:"), 0, 0)
      add(username, 1, 0)
      add(new Label("Password:"), 0, 1)
      add(password, 1, 1)
    }

    // Enable/Disable login button depending on whether a username was entered.
    val loginButton = dialog.dialogPane().lookupButton(loginButtonType)
    loginButton.disable = true

    // Do some validation (disable when username is empty).
    username.text.onChange { (_, _, newValue) => loginButton.disable = newValue.trim().isEmpty}

    dialog.dialogPane().content = grid

    // Request focus on the username field by default.
    Platform.runLater(username.requestFocus())

    // Convert the result to a username-password-pair when the login button is clicked.
    dialog.resultConverter = dialogButton =>
      if (dialogButton == loginButtonType) Result(username.text(), password.text())
      else null

    val result = dialog.showAndWait()

    result match {
      case Some(Result(u, p)) => println("Username=" + u + ", Password=" + p)
      case None               => println("Dialog returned: None")
    }
  }

}