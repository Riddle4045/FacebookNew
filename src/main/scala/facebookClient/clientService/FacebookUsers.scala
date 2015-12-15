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
import facebookClient.Client
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


class FacebookUsers(numOfUsers : Int,RequestRate : Int,publicKeyHashMap : collection.concurrent.Map[String,PublicKey]) extends Actor {
   
  implicit val system: ActorSystem = ActorSystem()
  implicit val timeout: Timeout = Timeout(15.seconds)
  import system.dispatcher
  val userName = self.path.name
  var privateKey : PrivateKey = null;
  
  def receive = {
    case StartClientRequests => distributeLoadAndSend
    case SendRequestToServer => sendRandomReuqest()
    case RegisterUser => registerUser
  }

  def registerUser = {
        println("registering " + userName + "...")
       var keyPairGen  = KeyPairGenerator.getInstance("RSA")
      var PublicPrivateKeyPair =keyPairGen.generateKeyPair();
      var publicKey = PublicPrivateKeyPair.getPublic
      var b64 = new BASE64Encoder()
      //var publicString = b64.encode(publicKey)
      publicKeyHashMap.put(userName, publicKey);
      privateKey = PublicPrivateKeyPair.getPrivate;
      
      //registration process..
      
      self ! StartClientRequests
  }
  
  def sendRandomReuqest() = {
      var act = generateRandomValue(6);
      var senderId = userName;
      var receiverId = generateRandomUserName();
      var message = generateRandomMessage();
      var profileField = generateFieldValue();
      act match {
        case 0 => updateProfile(senderId,profileField,message);
        case 1 =>  postToWall(senderId,receiverId,message);
        case 2 => addFriend(senderId, receiverId)
        case 3 => var albumName = generateRandomMessage()
          postPhoto(senderId, receiverId, message,albumName)
        case 4 => getWall
        case 5 => getProfile(userName)
      }
  }
  
  def distributeLoadAndSend = {
    val postRequestInterval = (RequestRate / 100000).milli
     println(postRequestInterval)
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
     // println(response.entity.data.asString)
    }  
  }
  
  def getWall = {
     for {
       response <- (IO(Http) ? HttpRequest(GET, Uri("http://127.0.0.1:8080/wall"))).mapTo[HttpResponse]
    }  yield {
    //  println(response.entity.data.asString)
    }    
  }
    def updateProfile(userId: String, profileField : String, profileValue:  String) = {
     var  EncryptedProfileValue :  String = RSA.encryptB64(publicKeyHashMap(userId), profileValue);
     var json_string = """{"userId" : " """  + userId + """ " , "profileField" : " """ + profileField + """ " , "profileValue" : " """ + EncryptedProfileValue + """" } """;
       var formData = HttpEntity(ContentType(spray.http.MediaTypes.`application/json`), json_string); 
    for {
       response <- (IO(Http) ? HttpRequest(POST, Uri("http://127.0.0.1:8080/updateProfile"), entity=formData)).mapTo[HttpResponse]  
    }  yield {
      //println(response.entity.data.asString)
    }  
  }

  def postToWall(senderId: String, receiverId : String, message:  String) = {
         var  EncryptedMessage :  String = RSA.encryptB64(publicKeyHashMap(receiverId), message);
         var json = """{"senderId" : " """  + senderId + """ " , "receiverId" : " """ + receiverId + """ " , "post" : " """ + EncryptedMessage + """" } """;
        // var json = """{ "post" : " """ + EncryptedMessage + """ "} """;
    //     println(json)
   var formData = HttpEntity(ContentType(spray.http.MediaTypes.`application/json`), json); 
    for {
      //response <- (IO(Http) ? HttpRequest(POST, Uri("http://127.0.0.1:8080/sendPost?senderId=" + senderId +"&receiverId=" + receiverId + "&post=" + EncryptedMessage), entity="""{ "key" : "whatever" }""")).mapTo[HttpResponse]          
   //  response <- (IO(Http) ? HttpRequest(POST, Uri("http://127.0.0.1:8080/sendPostwithData"), entity = formData).mapTo[HttpResponse]  
     response <- (IO(Http) ? HttpRequest(POST, Uri("http://127.0.0.1:8080/sendPostwithData"), entity=formData)).mapTo[HttpResponse]  
    }  yield {
   //   println(response.entity.data.asString) 
    }  
  }
  
  
  /*def receive = {
    case HttpRequest(GET, Uri.Path("/something"), _, _, _) =>
      sender ! HttpResponse(entity = """{ "key": "value" }""", headers = List(`Content-Type`(`application/json`)))
  }*/
  
 def addFriend(fromId : String, toId : String)={ 
      for {
       response <- (IO(Http) ? HttpRequest(POST, Uri("http://127.0.0.1:8080/addFriend?fromId=" + fromId +"&toId=" + toId))).mapTo[HttpResponse]
    }  yield {
     //println(response.entity.data.asString)
    }  
 }
 
  def postPhoto(fromId : String, toId : String, url : String, albumName : String)={
    
    var  EncryptedUrl :  String = RSA.encryptB64(publicKeyHashMap(toId), url);
    var  EncryptedAlbumName :  String = RSA.encryptB64(publicKeyHashMap(toId),albumName);
    var json = """{"fromId" : " """  + fromId + """ " , "toId" : " """ + toId + """ " , "url" : " """ + EncryptedUrl + """" , "albumName" : "  """+EncryptedAlbumName+""" "} """;
    var formData = HttpEntity(ContentType(spray.http.MediaTypes.`application/json`), json);  
    for {
       response <- (IO(Http) ? HttpRequest(POST, Uri("http://127.0.0.1:8080/postPhoto") ,entity=formData)).mapTo[HttpResponse]   
       } 
      yield {
     //println(response.entity.data.asString)
    }  
 }
  
    def generateRandomMessage(): String = {
    val random = new scala.util.Random
    val length: Int = random.nextInt(20) + 20 //min 20 chars, max 40 chars
    val sb = new StringBuilder
    for (i <- 1 to length) {
      //ASCII value 65 to 90 are printable characters
      sb.append((random.nextInt(25) + 65).toChar.toLower)
    }
    sb.toString
  }
    
  var profileFieldArray = Array("age" , "gender" , "school" , "university" , "interests", "movies" , "books");
  def generateFieldValue() :  String = {
   var random = new scala.util.Random
   var randomField = random.nextInt(profileFieldArray.length)
   return profileFieldArray(randomField);
  }

  
 
}