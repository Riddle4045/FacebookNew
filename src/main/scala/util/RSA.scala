package util

import java.security._
import java.security.spec.X509EncodedKeySpec
import javax.crypto._
import java.util.prefs.Base64
import sun.misc.BASE64Decoder
import sun.misc.BASE64Encoder

object RSA {
  def decodePublicKey(encodedKey: String): Option[PublicKey] = { 
    var b64_Decoder = new BASE64Decoder()
    this.decodePublicKey(
      b64_Decoder.decodeBuffer(encodedKey)
    )   
  }

  def decodePublicKey(encodedKey: Array[Byte]): Option[PublicKey] = { 
    scala.util.control.Exception.allCatch.opt {
      val spec = new X509EncodedKeySpec(encodedKey)
      val factory = KeyFactory.getInstance("RSA")
      factory.generatePublic(spec)
    }   
  }

  def encrypt(key: PublicKey, data: Array[Byte]): Array[Byte] = { 
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    cipher.doFinal(data)
  }

  def encryptB64(key: PublicKey, data: Array[Byte]): String = { 
    var b64_Encoder = new BASE64Encoder()
    b64_Encoder.encode(this.encrypt(key, data))
  }

  def encryptB64(key: PublicKey, data: String): String = { 
    this.encryptB64(key, data.getBytes)
  }
} 