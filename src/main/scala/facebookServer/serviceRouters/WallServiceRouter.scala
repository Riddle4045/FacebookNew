package facebookServer.serviceRouters

import akka.actor.Actor
import akka.actor.Props
import akka.routing.Router
import akka.routing.RoundRobinRoutingLogic
import akka.routing.ActorRefRoutee
import facebookServer.services.WallService
import scala.collection.mutable.MultiMap
import scala.collection.mutable.Set
import scala.collection.mutable.HashMap
import common.updateWall
import akka.actor.ActorRef

class WallServiceRouter(numberOfActors:Int, 
    WallHashMap: scala.collection.mutable.Map[String,HashMap[String,Set[String]] with MultiMap[String,String]],
    loadMonitor : ActorRef) extends Actor {
  var router = {
   val routees = Vector.fill(numberOfActors){
     val r = context.actorOf(Props(new WallService(numberOfActors,WallHashMap,loadMonitor)))
     context watch r
     ActorRefRoutee(r)
   }  
     Router(RoundRobinRoutingLogic(),routees) 
   }
     
        def receive = {
     case updateWall(receiverId,senderId,post) =>
       router.route(updateWall(receiverId,senderId,post),sender)
   }
      
}