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
import facebookClient.Client
import java.security.KeyPairGenerator
import java.util._
import sun.misc.BASE64Encoder


//This actor class will be sending only Get request 

class GETRequestClient(getRequestRate : Int, numOfUsers : Int,publicKeyHashMap : collection.concurrent.Map[String,String] ) extends Actor {
  
  implicit val system: ActorSystem = ActorSystem()
  implicit val timeout: Timeout = Timeout(15.seconds)
  import system.dispatcher
  generateKeyPair()
  
  def generateKeyPair(strength : Int = 1024)   = {
          var keyPairGen  = KeyPairGenerator.getInstance("RSA")
          var pair =  keyPairGen.generateKeyPair();
          var publicKey = pair.getPublic.getEncoded
          var privateKey = pair.getPrivate.getEncoded
          var b64 = new BASE64Encoder()
          var publicString = b64.encode(publicKey)
          
          
          //println(b64.encode(publicKey));


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
  
  def distributeLoadandSend = {
     val getrequestInterval = (getRequestRate / 100000).milli
     println(getrequestInterval)
    system.scheduler.schedule(0 milli, 100.microsecond, self.actorRef, SendRequestToServer)
  }
  
  def generateRandomValue(numberOfServices : Int) : Int = {  
    val r = scala.util.Random;
    return r.nextInt(numberOfServices);
  }

  def generateRandomUserName() : String = {
    var r = scala.util.Random
    var userId =  "user" +  r.nextInt(numOfUsers).toString()
    return userId
  }
  
  def receive = {
    case StartClientRequests => distributeLoadandSend
    case SendRequestToServer => 
      var userId = generateRandomUserName()
      var random = generateRandomValue(2)
      random match {
        case 0 => getProfile(userId);
        case 1 => getWall;
      }
    case _ => println("Unknown Request Intercepted by GETrequestActor")
  }
}