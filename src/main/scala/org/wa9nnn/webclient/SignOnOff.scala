package org.wa9nnn.webclient

import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.{formFields, onSuccess, path, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.CookieDirectives
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.Cell
import org.wa9nnn.fdcluster.authorization.PasswordManager
import org.wa9nnn.fdcluster.html
import org.wa9nnn.fdcluster.model.{AllContestRules, ContestProperty, Station}
import play.twirl.api.HtmlFormat

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

@Singleton
class SignOnOff @Inject()(contestProperty: ContestProperty,
                          passwordManager: PasswordManager,
                          allContestRules: AllContestRules,
                          sessionManagerSender: SessionManagerSender)
  extends CookieDirectives with LazyLogging {
  implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  val signonRoute: Route = {
    path("signon") {
      complete {

        HttpEntity(ContentTypes.`text/html(UTF-8)`, {
          val qsos: HtmlFormat.Appendable = html.Signon(allContestRules.currentRules)
          qsos.toString()
        }
        )
      }
    }
  }
  val logOutRoute: Route = {
    path("logout") {
      deleteCookie("session")
      redirect("/", StatusCodes.PermanentRedirect)
    }
  }


  val doSignonRoute: Route = {
    path("doSignon") {
      formFields("operator", "password", "band", "mode", "rig", "antenna") {
        (operator, password, band, mode, rig, antenna) =>
          val contestPassword = passwordManager.decrypt(contestProperty.contest.password)
          if (password == contestPassword) {
            onSuccess((
              sessionManagerSender ?[Session] Station(band, mode, operator, rig, antenna)
              ).mapTo[Session]) { session =>
              setCookie(
                HttpCookie("session", session.sessionKey)
              )(
                complete(
                  HttpEntity(ContentTypes.`text/html(UTF-8)`, {
                    html.QsoEntry(None, allContestRules.currentRules, session.station).toString()
                  }
                  )))
            }
          } else {
            HttpEntity(ContentTypes.`text/html(UTF-8)`, {
              val qsos: HtmlFormat.Appendable = html.Signon(allContestRules.currentRules, Option {
                Cell("Sorry you need the password. Check with the person who setup the system!")
                  .withCssClass("sad")
              })
              qsos.toString()
            }
            )
            complete(s"Sorry you need the password. Check with the person who setup the system!")
          }
      }
    }
  }


  val changeStation: Route = {
    path("changeStation") {
      cookie("session") { sessionCookie =>
        formFields("operator", "band", "mode", "rig", "antenna") {
          (operator, band, mode, rig, antenna) =>

            val newStation = Station(bandName = band,
              modeName = mode,
              operator = "dontcare here",
              rig = rig,
              antenna = antenna)
            val sessionKey = sessionCookie.value
            onSuccess(
              sessionManagerSender ? [Try[Session]]UpdateStation(sessionKey, newStation)) {
              case Failure(exception) =>
                complete(s"unknown sessionKey!")
              case Success(session) =>
                complete(
                  HttpEntity(ContentTypes.`text/html(UTF-8)`, {
                    html.QsoEntry(None, allContestRules.currentRules, session.station).toString()
                  }
                  )
                )
            }
        }
      }
    }
  }
}