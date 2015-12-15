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
import org.w3c.dom.Entity
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import org.json4s.JsonAST.JObject
import org.json4s.JsonFormat
import spray.json._
import spray.json.JsonParser
import scala.util.parsing.json.JSON
import org.json4s.JsonUtil
import scala.util.parsing.json.JSONType
import org.json4s.JsonWriter

object Master extends App with SimpleRoutingApp {
  
  def initInterface(actorSystem : ActorSystem) = {
        implicit val system = actorSystem
        var akkaServerPath ="akka://AkkaServer/user/"
        
    startServer(interface = "localhost" , port=8080){
        post {
            path("updateProfile") {
              entity(as[String])  { profile =>
                var data  = profile.stripMargin.replaceAll("[\n\r]","")
               var jsonData = data.parseJson.asJsObject
              var path = akkaServerPath + "profileServiceRouter"
                complete {
                          var postServiceRouter  = system.actorSelection(path) ! updateProfile(jsonData.getFields("userId").toString(),jsonData.getFields("profileField").toString(),jsonData.getFields("profileValue").toString())
                              "wall updated"
                }
              }      
            }
        } ~  
          post {
            path("sendPostwithData") {
              entity(as[String])  { post =>
                var data  = post.stripMargin.replaceAll("[\n\r]","")
               var jsonData = data.parseJson.asJsObject
            
                var path = akkaServerPath + "postServiceRouter"
                complete {
                     var postServiceRouter  = system.actorSelection(path) ! addPost(jsonData.getFields("receiverId").toString(),jsonData.getFields("senderId").toString(),jsonData.getFields("post").toString())
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
            /**/
        } ~
         post {
            path("postPhoto") {
            entity(as[String])  { photo =>
                var data  = photo.stripMargin.replaceAll("[\n\r]","")
               var jsonData = data.parseJson.asJsObject
                var path = akkaServerPath + "photoServiceRouter"
                complete {
                          var postServiceRouter  = system.actorSelection(path) ! addPhotos(jsonData.getFields("fromId").toString(),jsonData.getFields("toId").toString(),jsonData.getFields("url").toString(),jsonData.getFields("albumName").toString())
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