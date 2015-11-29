package facebookServer.services

import akka.actor.Actor
import scala.collection.mutable.MultiMap
import scala.collection.mutable.HashMap
import scala.collection.mutable.Set
import common.updateWall
import common.FinishedWork
import akka.actor.ActorRef




class WallService(numberOfActors :  Int , 
    WallHashMap: scala.collection.mutable.Map[String,HashMap[String,Set[String]] with MultiMap[String, String]],
    loadMonitor : ActorRef)  extends Actor{
  
  def updateFacebookWall(receiverId : String, senderId : String, post:  String) = {
    try{
    if(WallHashMap.isDefinedAt(receiverId)){
      var x  = WallHashMap.get(receiverId).get;
      x.addBinding(senderId, post)
    }
    else{
              var mm = new HashMap[String,Set[String]] with MultiMap[String,String]
              WallHashMap += receiverId -> mm
              WallHashMap.get(receiverId).get.addBinding(senderId, post)
    }
      
    }
    catch{
      case e : Exception => println("There's an exception")
    }
    loadMonitor ! FinishedWork("updateWall")
   // println("Wall " +WallHashMap.toString())
  }
  
  
  
   def receive = {
    case updateWall(receiverId,senderId,post)  =>
      updateFacebookWall(receiverId,senderId,post)
  }
}