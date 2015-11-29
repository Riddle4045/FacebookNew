import spray.routing.SimpleRoutingApp
import akka.actor.ActorSystem
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.routing.Directive.pimpApply
import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.json4s.ShortTypeHints
import spray.http.MediaType
import spray.http.MediaTypes
import com.sun.org.apache.bcel.internal.generic.ACONST_NULL
import common.addPost
import common.updateProfile
import common.addPost
import common.updateWall
import common.updateProfile
import common.FinishedWork
import common.addFriends
import common.addPhotos

object Master extends App with SimpleRoutingApp {
  
  def initInterface(actorSystem : ActorSystem) = {
        implicit val system = actorSystem
        var akkaServerPath ="akka://AkkaServer/user/"
        
    startServer(interface = "localhost" , port=8080){
        post {
            path("updateProfile") {
              parameters("userId","profileField" , "profileValue") { (userId ,profileField,profileValue) =>
                var path = akkaServerPath + "profileServiceRouter"
                complete {
                          var postServiceRouter  = system.actorSelection(path) ! updateProfile(userId,profileField,profileValue)
                              "wall updated"
                }
              }      
            }
        } ~
          post {
            path("sendPost") {
              parameters("senderId", "receiverId", "post") { (senderId,receiverId,post) =>
                var path = akkaServerPath + "postServiceRouter"
                complete {
                          var postServiceRouter  = system.actorSelection(path) ! addPost(receiverId,senderId,post)
                              "profile updated"
                }
              }


            }
        } ~  
        post {
            path("addFriend") {
              parameters("fromId", "toId") { (fromId,toId) =>
                var path = akkaServerPath + "friendListServiceRouter"
                complete {
                          var postServiceRouter  = system.actorSelection(path) ! addFriends(fromId,toId)
                              "friendList updated"
                }
              }
            }
        } ~
         post {
            path("postPhoto") {
              parameters("fromId", "toId", "url") { (fromId,toId,url) =>
                var path = akkaServerPath + "photoServiceRouter"
                complete {
                          var postServiceRouter  = system.actorSelection(path) ! addPhotos(fromId,toId,url)
                              "photo updated"
                }
              }
            }
        } ~
        get {
            path("profile") {
                   var path = akkaServerPath + "loadMonitorserivce"
                complete {
                  var loadMonitorservice = system.actorSelection(path) ! FinishedWork("getCall")
                    "Send get profile request"
                }
            }
        } ~
        get {
            path("wall")  { 
                  var path = akkaServerPath + "loadMonitorserivce"
              complete {
                     var loadMonitorservice = system.actorSelection(path) ! FinishedWork("getCall")
                  "Send wall in JSON form"
              }
            }
        }
    }
    
  }

   
}