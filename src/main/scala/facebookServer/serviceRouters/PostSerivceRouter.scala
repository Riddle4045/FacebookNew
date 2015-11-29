package facebookServer.serviceRouters


import akka.routing
import akka.actor.Actor
import akka.actor.Props
import scala.collection.mutable.HashMap
import akka.routing.Router
import akka.routing.RoundRobinRoutingLogic
import akka.routing.ActorRefRoutee
import facebookServer.services.PostService
import scala.collection.mutable.MultiMap
import scala.collection.mutable.Set
import common.addPost
import common.addPost
import akka.actor.ActorRef

class PostSerivceRouter(numberOfActors:Int, 
    PostHashMap: scala.collection.mutable.Map[String,HashMap[String,Set[String]] with MultiMap[String,String]],
    loadMonitor : ActorRef) extends Actor {
   var router = {
   val routees = Vector.fill(numberOfActors){
     val r = context.actorOf(Props(new PostService(numberOfActors, PostHashMap,loadMonitor )))
     context watch r
     ActorRefRoutee(r)
   }  
     Router(RoundRobinRoutingLogic(),routees) 
   }
   def receive = {
     case addPost(receiverId,senderId,post) =>
       router.route(addPost(receiverId,senderId,post),sender)
   }
   }