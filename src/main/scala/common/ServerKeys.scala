package common

import java.security.KeyPairGenerator
import java.security.KeyPair

object ServerKeys {
  
  var PublicPrivateKeyPair = generateServerkeys
  
         //public key of the server, used in authentication.
       val serverPublicKey = PublicPrivateKeyPair.getPublic    
      
      //private key of the server, used in authentication.
        val serverPrivateKey = PublicPrivateKeyPair.getPrivate;
      
  
  def generateServerkeys : KeyPair = {
          
       var keyPairGen  = KeyPairGenerator.getInstance("RSA")
       var PublicPrivateKeyPair =keyPairGen.generateKeyPair();
       return PublicPrivateKeyPair
  }

}