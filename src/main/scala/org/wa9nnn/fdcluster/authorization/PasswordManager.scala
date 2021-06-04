package org.wa9nnn.fdcluster.authorization

import com.github.racc.tscg.TypesafeConfig

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.{Inject, Singleton}

/**
 * Encrypt and decrypt the password.
 * Uses Blowfish algorithm. Not the best but no export restrictions.
 * And a Ham Radio field day logger doesn't exactly need high security.
 *
 * @param blowfishKey64 ogenerated using rg.wa9nnn.fdcluster.authorization.KeyGen
 */
@Singleton
class PasswordManager @Inject()(@TypesafeConfig("fdcluster.httpclient.blowfishKey64") blowfishKey64:String) {
  private val decoder = Base64.getDecoder
  private val encoder = Base64.getEncoder
  private def prepare(opMode:Int):Cipher = {
    val algorithm = "Blowfish"
    val cipher = Cipher.getInstance(algorithm)
    val bytes = decoder.decode(blowfishKey64)

    cipher.init(opMode, new SecretKeySpec(bytes, algorithm))
    cipher
  }
  def decrypt(encryted:String):String = {
    val cipher = prepare(Cipher.DECRYPT_MODE)
    new String(cipher.doFinal(decoder.decode(encryted)))
  }

  def encrypt(plainText:String):String = {
    val cipher = prepare(Cipher.ENCRYPT_MODE)
    encoder.encodeToString(cipher.doFinal(plainText.getBytes))
  }
}