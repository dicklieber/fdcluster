package org.wa9nnn.webclient

import akka.actor.ActorRef
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.{formFields, onSuccess, path, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.CookieDirectives
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import com.wa9nnn.util.tableui.Cell
import org.wa9nnn.fdcluster.authorization.PasswordManager
import org.wa9nnn.fdcluster.html
import org.wa9nnn.fdcluster.model.{AllContestRules, ContestProperty, Station}
import play.twirl.api.HtmlFormat

import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

@Singleton
class SignOnOff @Inject()(contestProperty: ContestProperty,
                          passwordManager: PasswordManager,
                          allContestRules: AllContestRules,
                          @Named("sessionManager") sessionManager: ActorRef)
  extends CookieDirectives with LazyLogging {
  implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  val signonRoute: Route = {
    path("signon") {
      complete {

        HttpEntity(ContentTypes.`text/html(UTF-8)`, {
          val qsos: HtmlFormat.Appendable = html.Signon()
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
      formFields("yourCallSign", "password") { (yourCallSign, password) =>
        val contestPassword = passwordManager.decrypt(contestProperty.contest.password)
        if (password == contestPassword) {
          onSuccess((
            sessionManager ? CreateSessionRequest(yourCallSign)
            ).mapTo[Session]) { session =>
            setCookie(HttpCookie("session", session.sessionKey))(
              complete(
                HttpEntity(ContentTypes.`text/html(UTF-8)`, {
                  html.QsoEntry(None, allContestRules.currentRules, session.station).toString()
                }
                )))
          }

        } else {
          HttpEntity(ContentTypes.`text/html(UTF-8)`, {
            val qsos: HtmlFormat.Appendable = html.Signon(Option {
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

            val newStation = Station(operator, band, mode, rig, antenna)
            val sessionKey = sessionCookie.value
            onSuccess(
              (sessionManager ? UpdateStation(sessionKey, newStation)).mapTo[Try[Session]]) { triedSession =>
              triedSession match {
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
}