package facebookServer.serviceRouters


import akka.routing
import akka.actor.Actor
import akka.actor.Props
import scala.collection.mutable.HashMap
import akka.routing.Router
import akka.routing.RoundRobinRoutingLogic
import akka.routing.ActorRefRoutee
import scala.collection.mutable.MultiMap
import scala.collection.mutable.Set
import common.addPhotos
import akka.actor.ActorRef
import facebookServer.services.PhotoService

class PhotoServiceRouter(numberOfActors:Int, 
    photoURLMap: scala.collection.mutable.Map[String,scala.collection.mutable.ListBuffer[String]],
    loadMonitor : ActorRef) extends Actor {
   var router = {
   val routees = Vector.fill(numberOfActors){
     val r = context.actorOf(Props(new PhotoService(numberOfActors, photoURLMap,loadMonitor )))
     context watch r
     ActorRefRoutee(r)
   }  
     Router(RoundRobinRoutingLogic(),routees) 
   }
   def receive = {
     case addPhotos(fromId, toId, url) =>
       router.route(addPhotos(fromId, toId, url),sender)
   }
   }