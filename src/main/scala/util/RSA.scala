package util

import java.security._
import java.security.spec.X509EncodedKeySpec
import javax.crypto._
import java.util.prefs.Base64
import sun.misc.BASE64Decoder
import sun.misc.BASE64Encoder
import sun.misc.IOUtils
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin
import com.sun.xml.internal.fastinfoset.algorithm.HexadecimalEncodingAlgorithm
import org.apache.commons.codec._
import org.apache.commons.codec.binary.Hex
import java.security.spec.PKCS8EncodedKeySpec
import org.apache.commons.codec.binary.Base64


object RSA {
  def decodePublicKey(encodedKey: String): Option[PublicKey] = { 
    var b64_Decoder = new BASE64Decoder()
    this.decodePublicKey(
      b64_Decoder.decodeBuffer(encodedKey)
    )   
  }

  def decodePublicKey(encodedKey: Array[Byte]): Option[PublicKey] = { 
    println("inside decodekey")
    scala.util.control.Exception.allCatch.opt {
      val spec = new X509EncodedKeySpec(encodedKey)
      val factory = KeyFactory.getInstance("RSA")
      factory.generatePublic(spec)
    }   
  }
  
  def myDecoder(key : String) : PublicKey= {
    println("Inside my decoder")
               var byteKey : Array[Byte] = Base64.decodeBase64(key.getBytes)
        var X509publicKey  : X509EncodedKeySpec = new X509EncodedKeySpec(byteKey);
        var kf : KeyFactory = KeyFactory.getInstance("RSA");

        return kf.generatePublic(X509publicKey);
  }
  
  
    def decodePrivateKey(encodedKey: String): Option[PrivateKey] = { 
    var b64_Decoder = new BASE64Decoder()
    this.decodePrivateKey(
      b64_Decoder.decodeBuffer(encodedKey)
    )   
  }
  
  def decodePrivateKey(encodedKey: Array[Byte]): Option[PrivateKey] = { 
    scala.util.control.Exception.allCatch.opt {
      val spec = new PKCS8EncodedKeySpec(encodedKey)
      val factory = KeyFactory.getInstance("RSA")
      factory.generatePrivate(spec)
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
  
 def decrypt64(Key : PrivateKey, data : String) : String = {
         val cipher = Cipher.getInstance("RSA")
         cipher.init(Cipher.DECRYPT_MODE, Key);
       //  println("Length of data : " + data.getBytes.length)
         var decoded_data = cipher.doFinal(data.getBytes)
       //  var decrypted = blockCipher(decoded_data,Cipher.DECRYPT_MODE,cipher);
         var ans  : String = new String(decoded_data,"UTF-8");
           return ans;
         
 }
 
 
 
// //++++++++++++++++++++++++++++++++++++++++++++++++
 def decrypt(Key : PrivateKey,encrypted : String) : String= {
          val cipher = Cipher.getInstance("RSA")
         cipher.init(Cipher.DECRYPT_MODE, Key);
	    var bts = Hex.decodeHex(encrypted.toCharArray());
      var decrypted = blockCipher(bts,Cipher.DECRYPT_MODE,cipher);
	    var ans  : String = new String(decrypted,"UTF-8");
	    return ans;
}
 
 
def blockCipher(bytes : Array[Byte],mode : Int, cipher : Cipher) : Array[Byte] = {
	// string initialize 2 buffers.
	// scrambled will hold intermediate results
	 var scrambled  : Array[Byte] = new Array[Byte](0);

	// toReturn will hold the total result
	var toReturn : Array[Byte] = new Array[Byte](0);
	// if we encrypt we use 100 byte long blocks. Decryption requires 128 byte long blocks (because of RSA)
	var length : Int= 0;
	if(mode == Cipher.ENCRYPT_MODE) {
	    length = 100;
	}else{
	    length = 1024;
	}
	// another buffer. this one will hold the bytes that have to be modified in this step
var buffer =new Array[Byte](length);

	for (i <- 0 until bytes.length) {

		// if we filled our buffer array we have our block ready for de- or encryption
		if ((i > 0) && (i % length == 0)){
			//execute the operation
		 scrambled = cipher.doFinal(buffer);
			// add the result to our total ult.
			toReturn = toReturn ++ scrambled
			// here we calculate the length of the next buffer required
			var newlength : Int = length;

			// if newlength would be longer than remaining bytes in the bytes array we shorten it.
			if (i + length > bytes.length) {
				 newlength = bytes.length - i;
			}
			// clean the buffer array
			buffer = new Array[Byte](newlength)
		}
		// copy byte into our buffer.
		var temp = i % length
		var x : Byte= bytes(i);
		buffer(temp) = x;
	}

	// this step is needed if we had a trailing buffer. should only happen when encrypting.
	// example: we encrypt 110 bytes. 100 bytes per run means we "forgot" the last 10 bytes. they are in the buffer array
	scrambled = cipher.doFinal(buffer);

	// final step before we can return the modified data.
	toReturn = toReturn ++ scrambled;

	return toReturn;
}

} 