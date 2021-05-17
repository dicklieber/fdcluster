package org.wa9nnn.fdcluster.javafx.entry

import org.wa9nnn.fdcluster.contest.ContestDialog
import scalafx.scene.control.{Hyperlink, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{BorderPane, HBox, VBox}

import java.io.InputStream
import javax.inject.Inject
import scala.util.Using

/**
 * Shown in [[EntryTab]] when app is not ready to log. e.i. No Contest or Journal.
 */
class NotReadyPane @Inject()(contestDialog: ContestDialog) extends BorderPane {
   val image: Image = Using(getClass.getResourceAsStream(s"/images/synchronize-cloud.gif")) { is: InputStream =>
    new Image(is, 50.0,50.0, true,true
    )}.get

  center = new HBox(
    new ImageView(image),
    new VBox(new Label("Not ready, setup or wait for another node to sync."),
      new Hyperlink("Contest Setup") {
        onAction = event => {
          contestDialog.show()
        }
      }
    ))
}
