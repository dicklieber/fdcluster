package about

//import _root_.scalafx.scene.control.{Hyperlink, _}

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.javafx.GridOfControls
import org.wa9nnn.fdcluster.{BuildInfo, FileContext, html}
import play.twirl.api.HtmlFormat
import scalafx.scene.control.{ButtonType, Dialog, Hyperlink}
import scalafx.scene.layout.VBox
import scalafx.scene.web._

import java.awt.Desktop
import java.net.URI
import javax.inject.{Inject, Singleton}
import scala.language.existentials

@Singleton
class AboutDialogHtml @Inject()(aboutTable: AboutTable,
                                fileManager: FileContext
                              ) extends Dialog with LazyLogging {

  title = s"About ${BuildInfo.name}"
  resizable = true
  private val cssUrl: String = getClass.getResource("/fdcluster.css").toExternalForm

  dialogPane.value.getButtonTypes.add(ButtonType.Close)
  implicit val desktop: Desktop = Desktop.getDesktop

  def apply(): Unit = {

    val goc = new GridOfControls()
    goc.addControl("App Directory", new Hyperlink(fileManager.directory.toString) {
      onAction = event => {
        desktop.open(fileManager.directory.toFile)
      }
    })
    goc.addControl("Log", new Hyperlink(fileManager.logFile.toString ){
      onAction = _ => {
        desktop.open(fileManager.logFile.toFile)
      }
    })
    goc.addControl("Blame this guy", new Hyperlink("Dick Lieber WA9NNN") {
      onAction = event => {
        if (desktop.isSupported(Desktop.Action.MAIL)) {
          val uri = s"mailto:${BuildInfo.maintainer}?subject=${BuildInfo.name}%20version:${BuildInfo.version}"
          val mailto = new URI(uri)
          desktop.mail(mailto)
        }
      }
    })



    val webView = new WebView()

    val asHtml = html.AboutDialog(aboutTable.apply).toString()

    val htmlCss: String = getClass.getResource("/css/main.css").toExternalForm

    val engine: WebEngine = webView.engine
    engine.userStyleSheetLocation = htmlCss
    engine.loadContent(asHtml)

    val dialogPane1 = dialogPane()
    dialogPane1.getStylesheets.add(cssUrl)
    dialogPane1.setContent(new VBox(
      webView,
      goc
    )
    )

    showAndWait()
  }
}

