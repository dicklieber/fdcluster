
package org.wa9nnn.fdcluster.adif

import org.specs2.mutable.Specification

class AdifQsoSpec extends Specification {
  "AdifQso" >> {
    val q1 = new AdifQso(Set(AdifEntry("CALL", "WA9NNN"),
      AdifEntry("XYZZY", "plugh"),
      AdifEntry("CLASS", "1H"),
    ))
    val q2 = new AdifQso(Set(AdifEntry("CALL", "WA9NNN"),
      AdifEntry("XYZZY", "plugh")))
    val r = q1.contains(q2)
    r must beTrue

  }
}
