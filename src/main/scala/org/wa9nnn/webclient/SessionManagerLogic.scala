package org.wa9nnn.webclient

import org.wa9nnn.fdcluster.model.Station
import org.wa9nnn.webclient.SessionManager.SessionKey

import java.security.SecureRandom
import java.time.{Duration, Instant}
import scala.collection.concurrent.TrieMap

class SessionManagerLogic(maxSessionSeconds: Long) {

  private val sessionMap: TrieMap[SessionKey, Session] = new TrieMap[SessionKey, Session]()

  def createSession(createSessionRequest: CreateSessionRequest): Session = {
    val newSession = Session(createSessionRequest)
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

//  def updateStation(sessionKey: SessionKey, station: Station): Try[Session] = {
//      Try{
//        val session = sessionMap(sessionKey).newStation(station)
//        sessionMap.put(sessionKey, session)
//        session
//      }
//  }

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
//  def newStation(station: Station): Session = {
//    val stationWithForcedOp = station.copy(operator = callSign)
//    copy(station = stationWithForcedOp)
//  }

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

  def apply(csr: CreateSessionRequest): Session = {
    val key = secureRandom.nextLong.toHexString
    val start = Instant.now()
    val callSign = csr.callSign
    new Session( key, Station(operator = callSign), start, start)
  }
}