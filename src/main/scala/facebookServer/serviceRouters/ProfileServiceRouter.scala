package facebookServer.serviceRouters


import akka.routing
import akka.actor.Actor
import akka.actor.Props
import akka.routing.Router
import akka.routing.RoundRobinRoutingLogic
import akka.routing.ActorRefRoutee
import facebookServer.services.ProfileService
import scala.collection.mutable.HashMap
import scala.collection.mutable.MultiMap
import scala.collection.mutable.Set
import common.updateProfile
import common.updateProfile
import akka.actor.ActorRef


/**
 * @author jdas
 */
class ProfileServiceRouter(numberOfActors: Int , userId : String,
    ProfileHashMap : scala.collection.mutable.Map[String,HashMap[String,Set[String]] with MultiMap[String,String]],
    loadMonitor: ActorRef) extends Actor {
  
  var router = {
   val routees = Vector.fill(numberOfActors){
     val r = context.actorOf(Props(new ProfileService(numberOfActors, userId, ProfileHashMap,loadMonitor )))
     context watch r
     ActorRefRoutee(r)
   }  
     Router(RoundRobinRoutingLogic(),routees) 
   }
   def receive = {
     case updateProfile(userId,profileField,profileValue) =>
       router.route(updateProfile(userId,profileField,profileValue),sender)
     
   }
}