package org.wa9nnn.webclient

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, Uri}
import akka.http.scaladsl.server.Directives.{extractUnmatchedPath, formFields, onSuccess, path, pathPrefix}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.CookieDirectives
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.Cell
import org.wa9nnn.fdcluster.html
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.store.{AddResult, DupQsoDetector, SearchResult, StoreSender}
import play.api.libs.json.Json
import play.twirl.api.HtmlFormat

import javax.inject.{Inject, Named, Singleton}
import scala.util.{Failure, Success}

@Singleton
class QsoLogger @Inject()(qsoMetadataProperty: OsoMetadataProperty,
                          storeSender: StoreSender,
                          allContestRules: AllContestRules,
                          sessionManagerSender: SessionManagerSender,
                          dupQsoDetector: DupQsoDetector,
                          contestProperty: ContestProperty) extends CookieDirectives with LazyLogging {
  val qsoEntryRoute: Route = {
    path("logQsos") {
      cookie("session") { sessionCookie =>
        onSuccess(
          sessionManagerSender ?[Option[Session]] RetriveSessionRequest(sessionCookie.value)) {
          case Some(session) =>
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, {
              val qsos: HtmlFormat.Appendable = html.QsoEntry(None, allContestRules.currentRules, session.station)
              qsos.toString
            }))
          case None =>
            complete(s"unknown sessionKey!")
        }
      }
    }
  }


  val logQsoRoute: Route = path("LogQso") {
    cookie("session") { sessionCookie =>
      formFields("callSign", "class", "section") { (callSign, clas, section) =>
        onSuccess(
          sessionManagerSender ?[Option[Session]] RetriveSessionRequest(sessionCookie.value)) {
          case Some(session) =>
            val station = session.station
            val potentialQso: Qso = qsoMetadataProperty.qso(
              callSign = callSign,
              exchange = Exchange(clas, section),
              station = session.station
            )
            if (potentialQso.callSign == contestProperty.callSign) {
              //todo reject
              complete(s"Can't work our own station: \n${potentialQso.callSign}!")
            }
            else {
              onSuccess(
                storeSender ?[AddResult] potentialQso
              ) { addResult ⇒
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

                    val qsos: HtmlFormat.Appendable = html.QsoEntry(Option(cell), allContestRules.currentRules, station)
                    qsos.toString()
                  }
                  )
                }
              }
            }

          case None =>
            complete(s"error no session!")
        }
      }
    }
  }

  val possibleDupRoute: Route = pathPrefix("dup") {
    cookie("session") { sessionCookie =>
      logger.debug(s"sessionCookie: ${sessionCookie.value}")
      onSuccess(sessionManagerSender ?[Option[Session]] (RetriveSessionRequest(sessionCookie.value))) { maybeSession: Option[Session] =>
        logger.debug(s"session: $maybeSession")
        extractUnmatchedPath { remaining: Uri.Path =>
          logger.debug(s"callSign: $remaining")
          val session = maybeSession.get
          onSuccess(dupQsoDetector(remaining.tail.toString, session.station.bandMode)) { searchResult: SearchResult =>
            logger.debug(s"searchResult: $searchResult")
            val appendable: HtmlFormat.Appendable = org.wa9nnn.fdcluster.html.PossibleDups(searchResult.possibleDups)
            val body = appendable.body
            complete(body)

          }
        }
      }
    }
  }
}

