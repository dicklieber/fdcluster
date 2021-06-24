package org.wa9nnn.fdcluster.contest.fieldday

import javafx.scene.control.DialogPane
import org.wa9nnn.fdcluster.BuildInfo
import org.wa9nnn.fdcluster.dupsheet.GenerateDupSheet
import scalafx.scene.control.{ButtonType, Dialog, Hyperlink, Label}
import scalafx.scene.layout.VBox

import java.awt.Desktop
import javax.inject.{Inject, Singleton}


class PostContestDialogFD @Inject()(summaryEngine: SummaryEngine, generateDupSheet: GenerateDupSheet,
                                   ) extends Dialog {
  title = s"About ${BuildInfo.name}"
  resizable = true
  private val cssUrl: String = getClass.getResource("/fdcluster.css").toExternalForm
  dialogPane.value.getButtonTypes.add(ButtonType.Close)

  implicit val desktop: Desktop = Desktop.getDesktop

  val help = new Label(
    """ARRL Field Day Submissions are done at https://field-day.arrl.org/fdentry.php. FdCluster provides information from the
      | log QSOs. Additional information is colleted at the ARRL Field Day entry site.
      | We'll create a file with information you need to copy to the web page and a dup sheet that needs to be sent to the ARRL in an email.
      |""".stripMargin) {
    styleClass += "helpPane"
  }
  val summarySheet = new Hyperlink("Create Summary.")
  summarySheet.onAction = { _ =>
    summaryEngine.invoke()
  }

  val dupSheet = new Hyperlink("Create Dup sheet file.")
  dupSheet.onAction = _ =>
    generateDupSheet.invoke()


  val dialogPane1: DialogPane = dialogPane()
  dialogPane1.getStylesheets.add(cssUrl)
  dialogPane1.setContent(new VBox(
    help,
    summarySheet,
    dupSheet
  ))

}
