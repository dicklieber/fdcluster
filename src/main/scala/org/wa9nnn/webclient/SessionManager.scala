package org.wa9nnn.webclient

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.Config
import org.wa9nnn.fdcluster.model.MessageFormats.CallSign
import org.wa9nnn.fdcluster.model.Station
import org.wa9nnn.webclient.SessionManager.SessionKey

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.reflect.ClassTag

class SessionManager(config: Config) extends Actor {
  val logic = new SessionManagerLogic(config.getDuration("fdcluster.httpclient.expireSessionsIn").toSeconds)
  context.system.getScheduler.scheduleAtFixedRate(10.seconds, 10.seconds, self, ExpireSessions)

  override def receive: Receive = {
    case station: Station =>
      sender ! logic.createSession(station)

    case RetriveSessionRequest(sessionKey) =>
      sender ! logic.getSession(sessionKey)

    case UpdateStation(sessionKey, station) =>
      sender ! logic.updateStation(sessionKey, station)

    case ExpireSessions =>
      logic.expireSessions()

    case ListSessions =>
      sender ! logic.listSessions
  }
}

@Singleton
class SessionManagerSender @Inject()(@Named("sessionManager") sessionManager: ActorRef) {
    private implicit val timeout: Timeout = Timeout(5, TimeUnit.SECONDS)

    def !(message: Any): Unit = {
      sessionManager ! message
    }

    def ?[T: ClassTag](message: Any): Future[T] = {
      (sessionManager ? message).mapTo[T]
    }

}

object SessionManager {
  type SessionKey = String
}

case object ExpireSessions

case object ListSessions

case class CreateSessionRequest(callSign: CallSign, station: Station = Station())

/**
 * replay with Option[Session]
 *
 * @param sessionKey fro, cookie.
 */
case class RetriveSessionRequest(sessionKey: SessionKey)

case class UpdateStation(sessionKey: SessionKey, station: Station)