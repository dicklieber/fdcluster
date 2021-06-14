package org.wa9nnn.webclient

import org.specs2.mutable.Specification
import org.wa9nnn.fdcluster.model.Station

import java.time.Instant

class SessionManagerLogicSpec extends Specification {
  val now: Instant = Instant.EPOCH
  val inetAddress = "10.10.10.10"

  "SessionManagerLogicSpec" >> {

    "createSession" >> {
      val sm = new SessionManagerLogic(25)
      val session: Session = sm.createSession(Station("WA9NNN"))

      session.station.operator must beEqualTo("WA9NNN")
      session.started must beEqualTo(session.last)

      val sessions = sm.listSessions
      sessions must haveLength(1)
      sessions.head must beEqualTo(session)

      val maybeSession = sm.getSession(session.sessionKey)
      val touched = maybeSession.get.last
      touched must be greaterThan (session.last)
    }

    "expireSessions" >> {
      val sm = new SessionManagerLogic(1)
      val session0: Session = sm.createSession(Station("WA9NNN"))
      val session1: Session = sm.createSession(Station("NE9A"))
      sm.listSessions must haveLength(2)
      Thread.sleep(2000)
      sm.expireSessions()
      sm.listSessions must haveLength(0)
    }
  }
}
