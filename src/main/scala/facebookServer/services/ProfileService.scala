package facebookServer.services
import scala.collection.mutable.HashMap
import akka.actor.Actor
import scala.collection.mutable.MultiMap
import scala.collection.mutable.Set
import common.updateProfile
import common.FinishedWork
import akka.actor.ActorRef


/**
 * @author jdas
 */
class ProfileService(numberOfActors: Int , userId:String, 
    ProfileHashMap: scala.collection.mutable.Map[String,HashMap[String,Set[String]] with MultiMap[String,String]],
    loadMonitor : ActorRef) extends Actor {
  
 def updateUserProfile(userId:String, profileField : String , profileValue : String) = {
    
    try{
      
      if(ProfileHashMap.isDefinedAt(userId)){
        var x = ProfileHashMap.get(userId).get;
       x.addBinding(profileField   , profileValue)
        
      }
      
         else{
              var mm = new HashMap[String,Set[String]] with MultiMap[String,String]
              ProfileHashMap += userId -> mm
              ProfileHashMap.get(userId).get.addBinding(profileField, profileValue)
    }
      
    }
      catch{
         case e : Exception => println(e);
      }
    loadMonitor ! FinishedWork("updateProfile")
  // println(ProfileHashMap.toString())
    
    
    
  }
  
  
   def receive = {
    case updateProfile(userId , profileField , profileValue) => updateUserProfile(userId , profileField, profileValue);
  }
}