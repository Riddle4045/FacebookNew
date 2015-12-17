package facebookClient.clientService

import util.RSA
import akka.actor.Actor
import spray.http.HttpResponse
import spray.can.Http
import spray.http.HttpRequest
import akka.io.IO
import akka.actor.ActorSystem
import scala.concurrent.duration._
import akka.util.Timeout
import shapeless.get
import spray.http.Uri
import spray.http._
import HttpMethods._
import akka.pattern.ask
import common.GetProfile
import common.GetTimeLine
import common.GetWall
import common.StartClientRequests
import common.SendRequestToServer
import java.security.KeyPairGenerator
import java.util._
import sun.misc.BASE64Encoder
import common.RegisterUser
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import spray.httpx.SprayJsonSupport
import spray.json._
import DefaultJsonProtocol._
import javax.crypto.SecretKey
import javax.crypto.KeyGenerator
import util.AES
import scala.concurrent.Await
import akka.pattern._
import scala.concurrent.Future
import org.apache.commons.codec._
import org.apache.commons.codec.binary.Base64
import util.AESnew



class FacebookUsers(numOfUsers : Int,RequestRate : Int,publicKeyHashMap : collection.concurrent.Map[String,PublicKey]) extends Actor {
   
  implicit val system: ActorSystem = ActorSystem()
  implicit val timeout: Timeout = Timeout(15.seconds)
  import system.dispatcher
  val userName = self.path.name
  var privateKey : PrivateKey = null;
  var aesKey : SecretKey = null;
  var serverPublicKey : PublicKey = null;
  var publicKey : PublicKey = null;
  
  def receive = {
    case StartClientRequests => distributeLoadAndSend
    case SendRequestToServer => sendRandomReuqest()
    case RegisterUser => registerUser
    case "GetKey" => getServerPublicKey
  }
  
  def generateUniversalAESkeys : SecretKey = {
//    println("INSIDE  GENERATE UNIVERSALKEY")
          // Generate key
    var kgen : KeyGenerator = KeyGenerator.getInstance("AES");
    kgen.init(128);
    var aesKey : SecretKey = kgen.generateKey();
    return aesKey
  }

  def registerUser = {

    println("registering " + userName + "...")
    
    //generate self's public and private key.
      var keyPairGen  = KeyPairGenerator.getInstance("RSA")
     //  keyPairGen.initialize(128)
      var PublicPrivateKeyPair =keyPairGen.generateKeyPair();
      var publicKey = PublicPrivateKeyPair.getPublic
      var b64 = new BASE64Encoder()
      //var publicString = b64.encode(publicKey)
      publicKeyHashMap.put(userName, publicKey);
      privateKey = PublicPrivateKeyPair.getPrivate;
      publicKey = PublicPrivateKeyPair.getPublic

      //registration process..
    
    //get the server's public key, which is being used to communicate since RSA kind of sucks.
    try {
      implicit val timeout: Timeout = Timeout(2.seconds)
      var getkey = self ? "GetKey"
      import system.dispatcher
      var sRes = Await.result(getkey, timeout.duration)

    } catch {
      case e: Exception => println("Still waiting for the Server's public key!")
    }
    //authenticate yourself to the Server.
    //here beings the journey of message exchanges which would seem retarded to a mere human 
    authenticateUser
    
    aesKey = generateUniversalAESkeys


  }
  
  def authenticateUser = {
    var selfPublicKeyString = Base64.encodeBase64String(publicKey.getEncoded)
    for {
       response <- (IO(Http) ? HttpRequest(GET, Uri("http://127.0.0.1:8080/authenticate?userId=" + userName + "&publickey=" + selfPublicKeyString ))).mapTo[HttpResponse]
    }  yield {
       println(response.entity.data.asString)
    }       
        startSendingRequessts
  }
  def startSendingRequessts  = {
    println("Starting to send request")
      self ! StartClientRequests
  }
  
  
  def sendRandomReuqest() = {
      var act = generateRandomValue(6);
      var senderId = userName;
      var receiverId = generateRandomUserName();
      var message = generateRandomMessage();
      var profileField = generateFieldValue();
   //   println("sending  request")
      act match {
        case 0 => updateProfile(senderId,profileField,message);
        case 1 => postToWall(senderId,receiverId,message);
        case 2 => addFriend(senderId, receiverId)
        case 3 => var albumName = generateRandomMessage()
          postPhoto(senderId, receiverId, message,albumName)
        case 4 => getWall
        case 5 => getProfile(userName)
      }
  }
  
  def distributeLoadAndSend = {
    val postRequestInterval = (RequestRate / 100000).milli
  //   println(postRequestInterval)
    system.scheduler.schedule(0 milli, 100.milli, self.actorRef, SendRequestToServer)
  }
    
    
  def generateRandomUserName() : String = {
    var r = scala.util.Random
    var userId =  "user" +  r.nextInt(numOfUsers).toString()
    return userId
  }
  
  def generateRandomValue(numberOfServices : Int) : Int = {  
    val r = scala.util.Random;
    return r.nextInt(numberOfServices);
  }

  def getProfile(userId: String) = {
    
    for {
       response <- (IO(Http) ? HttpRequest(GET, Uri("http://127.0.0.1:8080/profile"))).mapTo[HttpResponse]
    }  yield {
     println(response.entity.data.asString)
    }  
  }
  
  def getWall = {
     for {
       response <- (IO(Http) ? HttpRequest(GET, Uri("http://127.0.0.1:8080/wall"))).mapTo[HttpResponse]
    }  yield {
      println(response.entity.data.asString)

    }    
  }
  
  def getServerPublicKey = {
     for {
       response <- (IO(Http) ? HttpRequest(GET, Uri("http://127.0.0.1:8080/getServerPublicKey"))).mapTo[HttpResponse]
    }  yield {
     // println("GOT RESPONSE : " + response.entity.data.asString)
          var  serverPublicKeyString : String = response.entity.data.asString
       //   println("Decoding the String to Publickey")
        // serverPublicKey = RSA.decodePublicKey(serverPublicKeyString).asInstanceOf[PublicKey]
          serverPublicKey = RSA.myDecoder(serverPublicKeyString)
        //    println("ServerKey: " + serverPublicKey)
    //  startSendingRequessts
    }    
  }
  
    def updateProfile(userId: String, profileField : String, profileValue:  String) = {
    
      var aesKeyString : String = Base64.encodeBase64String(aesKey.getEncoded)
    
    var RSAencryptedProfileField = RSA.encryptB64(publicKeyHashMap(userId), profileField.getBytes)
    var RSAencryptedProfileValue = RSA.encryptB64(publicKeyHashMap(userId), profileValue.getBytes)
    
    //encrypt messages with the AES key.
    var AESencryptedRSAedProfileField  = AES.encrypt(aesKeyString, "iv34567891234678", RSAencryptedProfileField)
    var AESencryptedRSAedProfileValue = AES.encrypt(aesKeyString, "iv34567891234678", RSAencryptedProfileValue)
    
     //Encrypt AES key with RSA public Key for Server.
    var EncrpytAESkeywithRSAKey = RSA.encrypt(serverPublicKey, aesKey.getEncoded)
    
    //encode the encrypted key as base64 to send it over network.
    var b64_Encoder = new BASE64Encoder()
    var data =   b64_Encoder.encode(EncrpytAESkeywithRSAKey)

     var json_string = """{"userId" : " """  + userId + """ " , "profileField" : " """ + AESencryptedRSAedProfileField + """ " , "profileValue" : " """ + AESencryptedRSAedProfileValue + """", "EncryptedKey" : " """ + data + """ " } """;
       var formData = HttpEntity(ContentType(spray.http.MediaTypes.`text/plain`), json_string); 
    for {
       response <- (IO(Http) ? HttpRequest(POST, Uri("http://127.0.0.1:8080/updateProfile"), entity=formData)).mapTo[HttpResponse]  
    }  yield {
      println(response.entity.data.asString)
    }  
  }
    
    

  def postToWall(senderId: String, receiverId : String, message:  String) = {
    
    //convert AES key to Base64 Encoded String
    var aesKeyString : String = Base64.encodeBase64String(aesKey.getEncoded)
    
    var RSAencryptedMessage = RSA.encryptB64(publicKeyHashMap(receiverId), message.getBytes)
    //encrypt message with the AES key.
    var EncryptMessageWithAESKey  = AES.encrypt(aesKeyString, "iv34567891234678", RSAencryptedMessage)
    
     //Encrypt AES key with RSA public Key for Server.
    var EncrpytAESkeywithRSAKey = RSA.encrypt(serverPublicKey, aesKey.getEncoded)
    //encode the encrypted key as base64 to send it over network.
    var b64_Encoder = new BASE64Encoder()
    var data =   b64_Encoder.encode(EncrpytAESkeywithRSAKey)
    var json = """{"senderId" : " """  + senderId + """ " , "receiverId" : " """ + receiverId + """ " , "post" : " """ + EncryptMessageWithAESKey + """", "EncryptedKey" :" """ + data + """ " } """;
    var formData = HttpEntity(ContentType(spray.http.MediaTypes.`text/plain`), json); 
    for { 
     response <- (IO(Http) ? HttpRequest(POST, Uri("http://127.0.0.1:8080/sendPostwithData"), entity=formData)).mapTo[HttpResponse]  
    }  yield {
      println(response.entity.data.asString) 
    }  
  }

  
 def addFriend(fromId : String, toId : String)={ 
      for {
       response <- (IO(Http) ? HttpRequest(POST, Uri("http://127.0.0.1:8080/addFriend?fromId=" + fromId +"&toId=" + toId))).mapTo[HttpResponse]
    }  yield {
     println(response.entity.data.asString)
    }  
 }
 
  def postPhoto(fromId : String, toId : String, url : String, albumName : String)={
    
    var aesKeyString : String = Base64.encodeBase64String(aesKey.getEncoded)
    
    var RSAencryptedUrl = RSA.encryptB64(publicKeyHashMap(toId), url.getBytes)
    var RSAencryptedAlbumName = RSA.encryptB64(publicKeyHashMap(toId), albumName.getBytes)
    
    //encrypt messages with the AES key.
    var AESencryptedRSAedUrl  = AES.encrypt(aesKeyString, "iv34567891234678", RSAencryptedUrl)
    var AESencryptedRSAedAlbumName = AES.encrypt(aesKeyString, "iv34567891234678", RSAencryptedAlbumName)
    
     //Encrypt AES key with RSA public Key for Server.
    var EncrpytAESkeywithRSAKey = RSA.encrypt(serverPublicKey, aesKey.getEncoded)
    
    //encode the encrypted key as base64 to send it over network.
    var b64_Encoder = new BASE64Encoder()
    var data =   b64_Encoder.encode(EncrpytAESkeywithRSAKey)
    
    var json = """{"fromId" : " """  + fromId + """ " , "toId" : " """ + toId + """ " , "url" : " """ + AESencryptedRSAedUrl + """" , "albumName" : "  """+AESencryptedRSAedAlbumName+"""" , "EncryptedKey" : """" + data + """ "} """;
    var formData = HttpEntity(ContentType(spray.http.MediaTypes.`application/json`), json);  
    for {
       response <- (IO(Http) ? HttpRequest(POST, Uri("http://127.0.0.1:8080/postPhoto") ,entity=formData)).mapTo[HttpResponse]   
       } 
      yield {
     println(response.entity.data.asString)
    }  
 }
  
    def generateRandomMessage(): String = {
    val random = new scala.util.Random
    val length: Int = random.nextInt(1) + 2 //min 5 chars, max 10 chars
    val sb = new StringBuilder
    for (i <- 1 to length) {
      //ASCII value 65 to 90 are printable characters
      sb.append((random.nextInt(25) + 65).toChar.toLower)
    }
    sb.toString
  }
    
  var profileFieldArray = Array("age" , "gen" , "sch" , "uni" , "int", "mov" , "bok");
  def generateFieldValue() :  String = {
   var random = new scala.util.Random
   var randomField = random.nextInt(profileFieldArray.length)
   return profileFieldArray(randomField);
  }

  
 
}