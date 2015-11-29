package facebookServer.services

import akka.actor.Actor
import akka.actor.ActorRef
import common.addFriends
import common.FinishedWork
import common.addFriends
import common.addFriends

class FriendListService(numberOfActors: Int, 
    friendListMap :  scala.collection.mutable.Map[String,scala.collection.mutable.ListBuffer[String]],
    loadMonitor : ActorRef)  extends Actor{
 
  def receive = {
    case addFriends(fromId,toId) => 
    try {
            if(!friendListMap.isDefinedAt(fromId) || !friendListMap.isDefinedAt(toId)){
           // println("User inactive");
            }else{
                var addFromFriend = friendListMap.get(fromId)
                addFromFriend + (toId)
                var addToFriend = friendListMap.get(toId)
                addToFriend + (fromId)
    }
    loadMonitor ! FinishedWork("addFriend")
      }catch {
                case e : Exception => println("There's an exception in friendListService")
      }

  }
  
}