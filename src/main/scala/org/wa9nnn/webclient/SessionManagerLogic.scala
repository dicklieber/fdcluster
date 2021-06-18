package org.wa9nnn.webclient

import org.wa9nnn.fdcluster.model.Station
import org.wa9nnn.webclient.SessionManager.SessionKey

import java.security.SecureRandom
import java.time.{Duration, Instant}
import scala.collection.concurrent.TrieMap
import scala.util.Try

class SessionManagerLogic(maxSessionSeconds: Long) {


  private val sessionMap: TrieMap[SessionKey, Session] = new TrieMap[SessionKey, Session]()

  def createSession(station:Station): Session = {
    val newSession = Session(station)
    sessionMap.put(newSession.sessionKey, newSession)
    newSession
  }

  def getSession(sessionKey: SessionKey): Option[Session] = {
    sessionMap.get(sessionKey).map { s =>
      val session = s.touch()
      sessionMap.put(sessionKey, session)
      session
    }
  }

  def updateStation(sessionKey: SessionKey, station: Station): Try[Session] = {
      Try{
        val session = sessionMap(sessionKey).updateStation(station)
        sessionMap.put(sessionKey, session)
        session
      }
  }

  def expireSessions(): Unit = {
    val deathList: Iterable[Session] = sessionMap.values.flatMap { session =>
      Option.when(session.secondsOld > maxSessionSeconds)(
        session)

    }

    deathList.foreach { session => sessionMap.remove(session.sessionKey) }
  }

  def listSessions: List[Session] = {
    sessionMap
      .values
      .toList
      .sorted
  }
}

/**
 *
 * @param sessionKey unique id for this session. Lives in a cookie in the browser.
 * @param station use supplied info.
 * @param touched last time we dealt with this client session.
 * @param started when the session started.
 */
case class Session( sessionKey: SessionKey, station: Station, touched: Instant, started: Instant = Instant.now()) extends Ordered[Session] {

    def updateStation(newStation: Station): Session = {
    copy(station = newStation)
  }

  def last: Instant = {
    touched
  }

  def touch(): Session = {
    copy(touched = Instant.now())
  }


  def secondsOld: Long = {
    Duration.between(touched, Instant.now()).toSeconds
  }


  override def compare(that: Session): Int = station.operator.compareTo(that.station.operator)
}

object Session {
  private val secureRandom = new SecureRandom()

  def apply(station:Station): Session = {
    val key = secureRandom.nextLong.toHexString
    val start = Instant.now()
    new Session( key, station, start, start)
  }
}