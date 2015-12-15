package facebookServer.services

import akka.actor.Actor
import akka.actor.ActorRef
import common.addFriends
import common.FinishedWork
import common.addFriends
import common.addFriends
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

class FriendListService(numberOfActors: Int, 
    friendListMap :  scala.collection.mutable.Map[String,scala.collection.mutable.ListBuffer[String]],
    loadMonitor : ActorRef)  extends Actor{
 
  def receive = {
    case addFriends(fromId,toId) => 
    try {
            if(!friendListMap.isDefinedAt(fromId) || !friendListMap.isDefinedAt(toId)){
              
                if(!friendListMap.isDefinedAt(fromId)){
                  var listBuf = new ListBuffer[String]
                  friendListMap.put(fromId, listBuf)
                  var pp = friendListMap.get(fromId)
                  pp +  (toId)
                }else{
                           var addFromFriend = friendListMap.get(fromId)
                          addFromFriend + (toId)
                }
                
                
                if(!friendListMap.isDefinedAt(toId)){
                  var listBuf = new ListBuffer[String]
                  friendListMap.put(fromId, listBuf)
                  var pp = friendListMap.get(toId)
                  pp +  (fromId)
                }  else {
                  var addToFriend = friendListMap.get(toId)
                  addToFriend + (fromId)
              }
                
            }else{
                var addFromFriend = friendListMap.get(fromId)
                addFromFriend + (toId)
                var addToFriend = friendListMap.get(toId)
                addToFriend + (fromId)
    }
      //  println(friendListMap.toString())
    loadMonitor ! FinishedWork("addFriend")
      }catch {
                case e : Exception => println("There's an exception in friendListService")
      }

  }
  
}