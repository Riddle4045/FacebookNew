package facebookServer.services

import akka.actor.Actor
import akka.actor.ActorRef
import common.addFriends
import common.FinishedWork
import common.addPhotos
import scala.collection.mutable.HashMap
import scala.collection.mutable.MultiMap
import scala.collection.mutable.Set

class PhotoService(numberOfActors: Int,
    photoURLMap :  scala.collection.mutable.Map[String,HashMap[String,Set[String]] with MultiMap[String,String]],
    loadMonitor : ActorRef)  extends Actor{
 
  def receive = {
    case addPhotos(fromId,toId,url,albumName) => 
    try {
           if(photoURLMap.isDefinedAt(toId)){
      var x  = photoURLMap.get(toId).get;
      x.addBinding(url   , albumName)
    }
    else{
              var mm = new HashMap[String,Set[String]] with MultiMap[String,String]
              photoURLMap += toId -> mm
              photoURLMap.get(toId).get.addBinding(url, albumName)
    }
    
    //        println(photoURLMap.toString())
    loadMonitor ! FinishedWork("postPhoto")
      }catch {
                case e : Exception => println("There's an exception in PhotoServices")
      }

  }
  
}