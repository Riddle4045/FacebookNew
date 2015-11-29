package facebookServer.serviceRouters

import akka.actor.Actor
import akka.actor.Props
import facebookServer.services.FriendListService
import akka.routing.ActorRefRoutee
import akka.routing.RoundRobinRoutingLogic
import akka.routing.Router
import common.addFriends
import common.addFriends
import akka.actor.ActorRef

class FriendListRouter(numberOfActors:Int, 
    friendListMap :  scala.collection.mutable.Map[String,scala.collection.mutable.ListBuffer[String]],
    loadMonitor : ActorRef) extends Actor{
  
 var router = {
   val routees = Vector.fill(numberOfActors){
     val r = context.actorOf(Props(new FriendListService(numberOfActors, friendListMap,loadMonitor)))
     context watch r
     ActorRefRoutee(r)
   }  
     Router(RoundRobinRoutingLogic(),routees) 
 }
  
  def receive = {
    case addFriends(fromId,toId) =>
       router.route(addFriends(fromId,toId),sender)
  }
}