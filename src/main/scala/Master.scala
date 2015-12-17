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
import org.json4s.JsonUtil
import org.json4s.JsonWriter
import common.AuthenticateUser
import java.security.PublicKey
import common.GetServerPublicKey
import common.GetServerPrivateKey
import scala.concurrent.Await
import akka.util.Timeout
import util.RSA
import util.AES
import util.AESnew
import scala.concurrent._
import scala.concurrent.duration._
import akka.pattern._
import java.security.PrivateKey
import common.takePublicKey
import common.ServerKeys
import org.apache.commons.codec.binary.Base64
import sun.misc.BASE64Decoder
import javax.crypto.Cipher
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import scala.util.Success
import scala.util.Failure


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
                
                var en_str = data.substring(data.indexOf("EncryptedKey") + "EncryptedKey".length()+6)
                en_str = en_str.substring(0, en_str.length()-5)

                var profile_value_string = data.substring(data.indexOf("profileValue") + "profileValue".length() + 5,data.indexOf("EncryptedKey"))
                profile_value_string = profile_value_string.substring(0, profile_value_string.length-4)
                
                var profile_field_string = data.substring(data.indexOf("profileField") + "profileField".length() + 5,data.indexOf("profileValue"))
                profile_field_string = profile_field_string.substring(0, profile_field_string.length-5)

                var b64_decoder = new BASE64Decoder()
                var decodedKeyinBytes =  b64_decoder.decodeBuffer(en_str)
                val cipher = Cipher.getInstance("RSA")
                cipher.init(Cipher.DECRYPT_MODE, ServerKeys.serverPrivateKey);
                var decoded_data = cipher.doFinal(decodedKeyinBytes)
                var encoder : sun.misc.BASE64Encoder = new sun.misc.BASE64Encoder();
                var decrpytedKeyTodecryptPost  : String =  encoder.encode(decoded_data);
                var decryptedProfileField = AES.decrypt(decrpytedKeyTodecryptPost, "iv34567891234678", profile_field_string)
                 var decryptedProfileValue = AES.decrypt(decrpytedKeyTodecryptPost, "iv34567891234678", profile_value_string)
                complete {
                          var postServiceRouter  = system.actorSelection(path) ! updateProfile(jsonData.getFields("userId").toString(),decryptedProfileField,decryptedProfileValue)
                              "wall updated"
                }
              }      
            }
        } ~  
       post {
            path("sendPostwithData") {
              entity(as[String])  { post =>
                    var path = akkaServerPath + "postServiceRouter"
                    var privaKey = ServerKeys.serverPrivateKey
                    var data  = post.stripMargin.replaceAll("[\n\r\t' ']","")
                    
                    var jsonData = data.parseJson.asJsObject

                    var post_str = data.substring(data.indexOf("post") + "post".length() + 3,data.indexOf("EncryptedKey"))
                    post_str = post_str.substring(0, post_str.length-3)

                    var en_str = data.substring(data.indexOf("EncryptedKey") + "EncryptedKey".length()+3)

                    en_str = en_str.substring(0, en_str.length()-2)
     
                    var b64_decoder = new BASE64Decoder()
                    var decodedKeyinBytes =  b64_decoder.decodeBuffer(en_str)
                    val cipher = Cipher.getInstance("RSA")
                    cipher.init(Cipher.DECRYPT_MODE, ServerKeys.serverPrivateKey);
                    var decoded_data = cipher.doFinal(decodedKeyinBytes)
                    var encoder : sun.misc.BASE64Encoder = new sun.misc.BASE64Encoder();
                    var decrpytedKeyTodecryptPost  : String =  encoder.encode(decoded_data);
                    var decryptedData = AES.decrypt(decrpytedKeyTodecryptPost, "iv34567891234678", post_str)
                   
              complete {
                     var postServiceRouter  = system.actorSelection(path) ! addPost(jsonData.getFields("receiverId").toString(),jsonData.getFields("senderId").toString(),decryptedData)
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
            entity(as[String])  { photo =>
                var data  = photo.stripMargin.replaceAll("[\n\r]","")
                var jsonData = data.parseJson.asJsObject
                var path = akkaServerPath + "photoServiceRouter"
                var en_str = data.substring(data.indexOf("EncryptedKey") + "EncryptedKey".length()+5)
                en_str = en_str.substring(0, en_str.length()-4)

                var photo_url_string = data.substring(data.indexOf("url") + "url".length() + 5,data.indexOf("albumName"))
                photo_url_string = photo_url_string.substring(0, photo_url_string.length-5)
                
                var album_name_string = data.substring(data.indexOf("albumName") + "albumName".length() + 5,data.indexOf("EncryptedKey"))
                album_name_string = album_name_string.substring(0, album_name_string.length-5)
             
                var b64_decoder = new BASE64Decoder()
                var decodedKeyinBytes =  b64_decoder.decodeBuffer(en_str)
                val cipher = Cipher.getInstance("RSA")
                cipher.init(Cipher.DECRYPT_MODE, ServerKeys.serverPrivateKey);
                var decoded_data = cipher.doFinal(decodedKeyinBytes)
                var encoder : sun.misc.BASE64Encoder = new sun.misc.BASE64Encoder();
                var decrpytedKeyTodecryptPost  : String =  encoder.encode(decoded_data);
                var decryptedPhotoUrl = AES.decrypt(decrpytedKeyTodecryptPost, "iv34567891234678", photo_url_string)
                var decryptedAlbumName = AES.decrypt(decrpytedKeyTodecryptPost, "iv34567891234678", album_name_string)
             
                complete {
                          var postServiceRouter  = system.actorSelection(path) ! addPhotos(jsonData.getFields("fromId").toString(),jsonData.getFields("toId").toString(),jsonData.getFields("url").toString(),jsonData.getFields("albumName").toString())
                              "photo updated"
                }
              }
            }
        }~  get {
          path("getServerPublicKey"){
                   var path = akkaServerPath + "registrationAndAuthenticationService"
            complete {
                 //      implicit val timeout: Timeout = Timeout(1.seconds)
                 //     import system.dispatcher
                //  var registrationAndAuthenticationServiceRef = Await.result(system.actorSelection(path).resolveOne(),timeout.duration) 
              //  var fuckingFuture =  registrationAndAuthenticationServiceRef ? GetServerPublicKey
             //   val result = Await.result(fuckingFuture, timeout.duration)
          //      result.toString()
           var serverPublicKey =   ServerKeys.serverPublicKey
           var serverPublicKeyString = Base64.encodeBase64String(serverPublicKey.getEncoded)
           serverPublicKeyString
            }
        }
     }  ~  get {
          path("authenticate"){
                parameters("userId", "publickey") { (userId,publickey) =>
                var b64_decoder = new BASE64Decoder()
                var decodedKeyinBytes =  b64_decoder.decodeBuffer(publickey)
                var   pubK : PublicKey =   KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decodedKeyinBytes));
                var path = akkaServerPath + "registrationAndAuthenticationService"
            complete {
                 implicit val timeout: Timeout = Timeout(1.seconds)
                import system.dispatcher
              var f  = system.actorSelection(path)  ? AuthenticateUser(userId,pubK);
              f.onComplete { 
                  case Success(value) => println("Printing the value returned for authentication: " + value)
                  case Failure(e) => e.printStackTrace
              }
              "authenticaing..."
            }
          }
        }
     } ~ get {
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