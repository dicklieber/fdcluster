package org.wa9nnn.webclient

import akka.actor.Actor
import com.typesafe.config.Config
import org.wa9nnn.fdcluster.model.MessageFormats.CallSign
import org.wa9nnn.fdcluster.model.Station
import org.wa9nnn.webclient.SessionManager.SessionKey

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class SessionManager(config: Config) extends Actor {
  val logic = new SessionManagerLogic(config.getDuration("fdcluster.httpclient.expireSessionsIn").toSeconds)
  context.system.getScheduler.scheduleAtFixedRate(10.seconds, 10.seconds, self, ExpireSessions)

  override def receive: Receive = {
    case csr:CreateSessionRequest =>
      sender ! logic.createSession(csr)

    case RetriveSessionRequest(sessionKey) =>
      sender ! logic.getSession(sessionKey)

//    case UpdateStation(sessionKey, station) =>
//      sender ! logic.updateStation(sessionKey, station)

    case ExpireSessions =>
      logic.expireSessions()

    case ListSessions =>
      sender ! logic.listSessions
  }
}

object SessionManager {
  type SessionKey = String
}

case object ExpireSessions

case object ListSessions

case class CreateSessionRequest(callSign: CallSign)

/**
 * replay with Option[Session]
 *
 * @param sessionKey fro, cookie.
 */
case class RetriveSessionRequest(sessionKey: SessionKey)

case class UpdateStation(sessionKey:SessionKey, station:Station)