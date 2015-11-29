package facebookClient.clientService

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
import common.PostToProfile
import common.PostToTimeLine
import common.PostToWall


//This actor class will be sending only Get request 
class POSTRequestClient(postRequestRate : Int, numOfUsers : Int) extends Actor {
  
  implicit val system: ActorSystem = ActorSystem()
  implicit val timeout: Timeout = Timeout(15.seconds)
  import system.dispatcher
  
  def updateProfile(userId: String, profileField : String, profileValue:  String) = {
    for {
       response <- (IO(Http) ? HttpRequest(POST, Uri("http://127.0.0.1:8080/updateProfile?userId=" + userId +"&profileField=" + profileField + "&profileValue=" + profileValue))).mapTo[HttpResponse]
    }  yield {
      //println(response.entity.data.asString)
    }  
  }
  
  def postToWall(senderId: String, receiverId : String, message:  String) = {
    for {
       response <- (IO(Http) ? HttpRequest(POST, Uri("http://127.0.0.1:8080/sendPost?senderId=" + senderId +"&receiverId=" + receiverId + "&post=" + message))).mapTo[HttpResponse]
    }  yield {
      //println(response.entity.data.asString)
    }  
  }
  
 def addFriend(fromId : String, toId : String)={ 
      for {
       response <- (IO(Http) ? HttpRequest(POST, Uri("http://127.0.0.1:8080/addFriend?fromId=" + fromId +"&toId=" + toId))).mapTo[HttpResponse]
    }  yield {
     //println(response.entity.data.asString)
    }  
 }
 
  def postPhoto(fromId : String, toId : String, url : String)={ 
      for {
       response <- (IO(Http) ? HttpRequest(POST, Uri("http://127.0.0.1:8080/postPhoto?fromId=" + fromId +"&toId=" + toId + "&url=" + url))).mapTo[HttpResponse]
    }  yield {
     //println(response.entity.data.asString)
    }  
 }
  
  def distributeLoadandSend = {
     val postRequestInterval = (postRequestRate / 100000).milli
     println(postRequestInterval)
    system.scheduler.schedule(0 milli, 100.microseconds, self.actorRef, SendRequestToServer)
  }
  
  def generateRandomValue(numberOfServices : Int) : Int ={
    val r = scala.util.Random;
    return r.nextInt(numberOfServices);
  }
  
   def generateRandomUserName() : String = {
    var r = scala.util.Random
    var userId =  "user" +  r.nextInt(numOfUsers).toString()
    return userId
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

  def receive = {
    case StartClientRequests => distributeLoadandSend
    case SendRequestToServer => 
      var random = generateRandomValue(4)
      var senderId = generateRandomUserName();
      var receiverId = generateRandomUserName();
      var message = generateRandomMessage();
      var profileField = generateFieldValue();
      random match {
        case 0 => updateProfile(senderId,profileField,message);
        case 1 => postToWall(senderId,receiverId,message);
        case 2 => addFriend(senderId, receiverId)
        case 3 => postPhoto(senderId, receiverId, message)
      }
    case _ => println("Unknown Request Intercepted by GETrequestActor")
  }
}