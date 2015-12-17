package util

  import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Base64;
import java.security.MessageDigest
import java.util


object AES {

  def encrypt( key : String ,  initVector : String,value : String) : String =  {
        try {
            var iv : IvParameterSpec = new IvParameterSpec(initVector.getBytes("UTF-8"));
          //  var skeySpec : SecretKeySpec= new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            var skeySpec : SecretKeySpec = keyToSpec(key)
          //    println("Size of keyspec : " + skeySpec.getEncoded.length)
            var cipher  : Cipher= Cipher.getInstance("AES/CBC/PKCS5PADDING");
            
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            var encrypted : Array[Byte] = cipher.doFinal(value.getBytes());
//            System.out.println("encrypted string: "
//                    + Base64.encodeBase64String(encrypted));

            return Base64.encodeBase64String(encrypted);
        } catch{
          case e  : Exception=>  e.printStackTrace()
          
        return null;
        }
        

  }

 def decrypt( key : String ,initVector : String,encrypted :String) : String ={
        try {
            var  iv : IvParameterSpec = new IvParameterSpec(initVector.getBytes("UTF-8"));
          //   var skeySpec : SecretKeySpec= new SecretKeySpec(key.getBytes("UTF-8"), "AES");
             var skeySpec : SecretKeySpec= keyToSpec(key)
            var  cipher : Cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            var original : Array[Byte] = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
        } catch  {
          case e : Exception =>  e.printStackTrace()
                  return null;
        }


    }
     private val SALT: String =
    "jMhKlOuJnM34G6NHkqo9V010GhLAqOpF0BePojHgh1HgNg8^72k"
    
    def keyToSpec(key: String): SecretKeySpec = {
    var keyBytes: Array[Byte] = (SALT + key).getBytes("UTF-8")
    val sha: MessageDigest = MessageDigest.getInstance("SHA-1")
    keyBytes = sha.digest(keyBytes)
    keyBytes = util.Arrays.copyOf(keyBytes, 16)
    new SecretKeySpec(keyBytes, "AES")
  }


}

