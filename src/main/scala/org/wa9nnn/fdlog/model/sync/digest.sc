
import java.security.MessageDigest

val messageDigest = MessageDigest.getInstance("SHA-256")
messageDigest.update("one".getBytes)
messageDigest.update("two".getBytes)
messageDigest.update("three".getBytes)
val digestBinary = messageDigest.digest()
val sDigest = java.util.Base64.getEncoder.encode(digestBinary)
val s = sDigest.toString


java.util.Base64.getDecoder.decode(s.getBytes())