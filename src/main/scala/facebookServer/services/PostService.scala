package facebookServer.services
import scala.collection.mutable.HashMap
import akka.actor.Actor
import scala.collection.mutable.MultiMap
import scala.collection.mutable.Set
import common.addPost
import common.updateWall
import common.FinishedWork
import akka.actor.ActorRef

class PostService(numberOfActors :  Int , 
    PostHashMap: scala.collection.mutable.Map[String,HashMap[String,Set[String]] with MultiMap[String, String]],
    loadMonitor : ActorRef)  extends Actor{
  

  def updateUserPosts(receiverId : String, senderId : String, post:  String) = {
    try{
    if(PostHashMap.isDefinedAt(receiverId)){
      var x  = PostHashMap.get(receiverId).get;
      x.addBinding(senderId, post)
    }
    else{
              var mm = new HashMap[String,Set[String]] with MultiMap[String,String]
              PostHashMap += receiverId -> mm
              PostHashMap.get(receiverId).get.addBinding(senderId, post)
    }

    }
    catch{
      case e : Exception => println("There's an exception")
    }
  //  println("printing post" + PostHashMap.toString())
    updateFacebookWall(receiverId,senderId,post)
    loadMonitor ! FinishedWork("addPost")
  }
  
  def updateFacebookWall(receiverId :String , senderId : String , post : String){
        var wallServiceRouterPath  = "akka://AkkaServer/user" +  "/wallServiceRouter" 
        var wallServiceRouter = context.actorSelection(wallServiceRouterPath) ! updateWall(receiverId,senderId,post)
  }
  
  
  
 // receiverId : {senderId,Message}
  def receive = {
    case addPost(receiverId,senderId,post)  =>
      updateUserPosts(receiverId,senderId,post)
  }
}