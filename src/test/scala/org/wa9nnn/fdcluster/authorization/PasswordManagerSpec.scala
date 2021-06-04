package org.wa9nnn.fdcluster.authorization

import org.specs2.mutable.Specification

import java.util.Base64

class PasswordManagerSpec extends Specification {
val passwordManager = new PasswordManager("2xHUNvmX/JH8d2IpGuh+Pw==")
  "PasswordManagerSpec" >> {
    "roundtrip" >> {
      val plainText = "CQ CQ CQ DE WA9NNN"
      val encyrpted64 = passwordManager.encrypt(plainText)
      val not64encoded = new String(Base64.getDecoder.decode(encyrpted64))
      not64encoded must not be_==(plainText) // make sure it's really encryptednot just base64 encoded!
      val backAgain = passwordManager.decrypt(encyrpted64)
      backAgain must beEqualTo (plainText)
    }
  }
}
