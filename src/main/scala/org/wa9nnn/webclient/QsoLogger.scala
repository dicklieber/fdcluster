package org.wa9nnn.webclient

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{formFields, onSuccess, path}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.Cell
import org.wa9nnn.fdcluster.html
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.store.{AddResult, StoreSender}
import play.twirl.api.HtmlFormat

import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success}

@Singleton
class QsoLogger @Inject()(qsoBuilder: QsoBuilder,
                          storeSender: StoreSender,
                          allContestRules: AllContestRules,
                          contestProperty: ContestProperty) extends LazyLogging {
  val qsoEntryRoute:Route = {
    path("logQsos") {
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, {
        val qsos: HtmlFormat.Appendable = html.QsoEntry(None, allContestRules.currentRules, Station())
        qsos.toString()
      }
      ))

    }
  }

  val logQsoRoute: Route = path("LogQso") {
    formFields("callSign", "class", "section") { (callSign, clas, section) =>

      val potentialQso: Qso = qsoBuilder.qso(
        callSign = callSign,
        exchange = Exchange(clas, section),
        bandMode = BandMode() //"todo"
      )
      if (potentialQso.callSign == contestProperty.callSign) {

        //todo reject
        complete(s"Can't work our own station: \n${potentialQso.callSign}!")
      }
      else {
        onSuccess((
          storeSender ?[AddResult] potentialQso
          ).mapTo[AddResult]) { addResult ⇒
          complete {
            HttpEntity(ContentTypes.`text/html(UTF-8)`, {
              val cell: Cell = addResult.triedQso match {
                case Failure(exception) =>

                  Cell(exception.getMessage)
                    .withCssClass("sadQso")
                case Success(qso) =>
                  Cell(s"Added ${qso.callSign}")
                    .withCssClass("happyQso")
              }

              val qsos: HtmlFormat.Appendable = html.QsoEntry(Option(cell), allContestRules.currentRules, Station())
              qsos.toString()
            }
            )
          }
        }
      }
    }
  }
}

