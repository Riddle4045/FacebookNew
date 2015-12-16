package facebookServer.services

import akka.actor.Actor
import common.RequestServerPublicKey
import java.security.PublicKey
import java.security.PrivateKey
import common.AuthenticateUser
import util.RSA
import sun.misc.BASE64Encoder
import java.security
import common.ConfirmAuthentication
import common.GetServerPublicKey
import common.takePublicKey



class RegistrationsAndAuthenticationService(serverPublicKey :  PublicKey, serverPrivateKey : PrivateKey,sessionIdMap : collection.concurrent.Map[String,String]) extends Actor {
  
  def authenticateUser(userId : String, publickey : PublicKey)  : String = {
    
          //generate a rondom number
          //encrypt the random number using the pub-key.
          //send it back to the user.
                var num = userId.split("r");
                var random  = num(1).toInt + 1;
        var  EncryptedRandomValue :  String = RSA.encryptB64(publickey, random.toString());
        sessionIdMap.put(userId,EncryptedRandomValue);
        return EncryptedRandomValue;
        
    //store the random number against user, to authenticate.
    
  }
  
  def confirmAuthentication(userId : String , randomNumber :  String) :  Boolean = {
    
    try{
    if(!sessionIdMap.contains(userId)){
      return false;
    }
    
    if(sessionIdMap.get(userId).equals(randomNumber)){
      return true;
    }
    else
      return false;
    } catch{
                  case e : Exception => 
                    println("Exception in authenticating user");
                    return false;
  }
  }
  def receive = {
    case RequestServerPublicKey => sender ! serverPublicKey
    case AuthenticateUser(userId,publickey) => sender ! authenticateUser(userId,publickey) 
    case ConfirmAuthentication(userId,randomNumber) => sender ! confirmAuthentication(userId,randomNumber)
    case GetServerPublicKey => println("Got a message requesting public key which is " + serverPublicKey)    
      sender ! serverPublicKey
  }
}