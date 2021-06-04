package org.wa9nnn.fdcluster.authorization

import java.util.Base64
import javax.crypto.{Cipher, SecretKey}
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object KeyGen extends App {

  import javax.crypto.KeyGenerator

  private val secretKey: SecretKey = KeyGenerator.getInstance("Blowfish").generateKey()
  private val encoder64: Base64.Encoder = Base64.getEncoder
  private val blowfishKey64: String = encoder64.encodeToString(secretKey.getEncoded)
  println(s"new BlowfishKey64: $blowfishKey64")

}


