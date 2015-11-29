package facebookClient.clientService

import akka.actor.Actor
import common.StartClientRequests
import akka.io.IO
import spray.can.Http
import scala.concurrent.Future
import akka.util.Timeout
import akka.pattern.ask
import spray.http._
import HttpMethods._
import akka.actor.ActorSystem
import scala.concurrent.duration._
import common.SendRequestToServer
import akka.actor.Props
import scala.concurrent.duration
import akka.actor.ActorRef

class ClientService(numOfUsers : Int,getRequestRate : Int,postRequestRate : Int, actorsystem : ActorSystem) extends Actor {
  
  implicit val system: ActorSystem = actorsystem
  implicit val timeout: Timeout = Timeout(15.seconds)
  import system.dispatcher
  
//  def startSendingRequests = {
//    for {
//     response <- (IO(Http) ? HttpRequest(GET, Uri("http://127.0.0.1:8080/working"))).mapTo[HttpResponse]
//    }  yield {
//      if(count % 100 == 0){
//        println(count)}    
//        count = count + 1;   
//    }  
//  }

  var getRequestActor = new Array[ActorRef](100);
  var postRequestActor = new Array[ActorRef](100);
  
  def scheduleRequest = {
    for ( i <- 1 to 100){
         getRequestActor(i) = system.actorOf(Props( new GETRequestClient(getRequestRate,numOfUsers)), name="getRequestActor" + i) 
           getRequestActor(i) ! StartClientRequests
     postRequestActor(i) = system.actorOf(Props(new POSTRequestClient(postRequestRate,numOfUsers)), name="postRequestActor" + i) 
     postRequestActor(i) ! StartClientRequests 
    }

  }
  
  
  def receive = {  
    case StartClientRequests => println("Scheduling Requests")
      scheduleRequest
  }
}