package facebookServer.services

import akka.actor.Actor
import akka.actor.ActorRef
import common.addFriends
import common.FinishedWork
import common.addPhotos

class PhotoService(numberOfActors: Int,
    photoURLMap :  scala.collection.mutable.Map[String,scala.collection.mutable.ListBuffer[String]],
    loadMonitor : ActorRef)  extends Actor{
 
  def receive = {
    case addPhotos(fromId,toId,url) => 
    try {
            if(!photoURLMap.isDefinedAt(fromId) || !photoURLMap.isDefinedAt(toId)){
           // println("User inactive");
            }else{
               var photos = photoURLMap.get(toId)
               photos  + (toId)
               var fromList = photoURLMap(fromId)
               fromList + (toId)
    }
    loadMonitor ! FinishedWork("postPhoto")
      }catch {
                case e : Exception => println("There's an exception in PhotoServices")
      }

  }
  
}