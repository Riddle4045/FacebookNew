import akka.remote
import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent
import scala.collection.convert.decorateAsScala.mapAsScalaConcurrentMapConverter
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import scala.collection.mutable.HashMap
import facebookServer.serviceRouters.PostSerivceRouter
import facebookServer.serviceRouters.ProfileServiceRouter
import facebookServer.serviceRouters.WallServiceRouter
import common.Constants
import java.util.concurrent.ConcurrentMap
import scala.collection.mutable.MultiMap
import scala.collection.mutable.Set
import facebookServer.services.LoadMonitorService
import facebookClient.Client
import facebookServer.serviceRouters.FriendListRouter
import facebookServer.serviceRouters.PhotoServiceRouter
import util.RSA
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.KeyPairGenerator

object Server {
  //this will be the entry point for the server. 
  //will contain all the hashMaps which will be passed as a reference.
  def main(args : Array[String]) {
    
    
    
    
       var keyPairGen  = KeyPairGenerator.getInstance("RSA")
       var PublicPrivateKeyPair =keyPairGen.generateKeyPair();
       
       //public key of the server, used in authentication.
       val serverPublicKey = PublicPrivateKeyPair.getPublic    
      
      //private key of the server, used in authentication.
        val severPrivateKey = PublicPrivateKeyPair.getPrivate;
      
      
        val constants = new Constants()
        val PostHashMap : concurrent.Map[String,HashMap[String,Set[String]] with MultiMap[String,String]] = new ConcurrentHashMap().asScala
        val WallHashMap : concurrent.Map[String,HashMap[String,Set[String]] with MultiMap[String,String]] = new ConcurrentHashMap().asScala
        val ProfileHashMap : concurrent.Map[String,HashMap[String,Set[String]] with MultiMap[String,String]] = new ConcurrentHashMap().asScala
        val friendListMap : concurrent.Map[String,scala.collection.mutable.ListBuffer[String]] = new ConcurrentHashMap().asScala
        val photoURLMap : concurrent.Map[String,HashMap[String,Set[String]] with MultiMap[String,String]] = new ConcurrentHashMap().asScala
        println("running project")

        var hostAddress : String  = java.net.InetAddress.getLocalHost.getHostAddress() 
      /**  val configString = """akka {
            actor {
              provider = "akka.remote.RemoteActorRefProvider"
            }
            remote {
              enabled-transports = ["akka.remote.netty.tcp"]
              netty.tcp {
                hostname = """ + hostAddress + """
                port = """ + constants.AKKA_SERVER_PORT + """
              }
           }
          }"""
        val configuration = ConfigFactory.parseString(configString)**/
     //   val system = ActorSystem("AkkaServer",ConfigFactory.load(configuration))
        val system = ActorSystem("AkkaServer")
         
        //to start the services
        startServices(system,PostHashMap,ProfileHashMap,WallHashMap,friendListMap,photoURLMap,severPrivateKey)
        println("starting services")
        Master.initInterface(system)
        
  }
 
  
  def startServices(system : ActorSystem , PostHashMap : scala.collection.mutable.Map[String,HashMap[String,Set[String]] with MultiMap[String,String]], 
      ProfileHashMap : scala.collection.mutable.Map[String,HashMap[String,Set[String]] with MultiMap[String,String]],
      WallHashMap : scala.collection.mutable.Map[String,HashMap[String,Set[String]] with MultiMap[String,String]],
      FriendListMap : scala.collection.mutable.Map[String,scala.collection.mutable.ListBuffer[String]],
      photoURLMap : scala.collection.mutable.Map[String,HashMap[String,Set[String]] with MultiMap[String,String]], severPrivateKey : PrivateKey) = {
    
    val loadMonitorserivce = system.actorOf(Props(new LoadMonitorService(system)), name="loadMonitorserivce")
    val postServiceRouter = system.actorOf(Props(new PostSerivceRouter(15,PostHashMap,loadMonitorserivce)), name="postServiceRouter");
    val profileServiceRouter = system.actorOf(Props(new ProfileServiceRouter(15,"userId", ProfileHashMap,loadMonitorserivce)), name = "profileServiceRouter");
    val wallServiceRouter = system.actorOf(Props(new WallServiceRouter(15,WallHashMap,loadMonitorserivce)),name = "wallServiceRouter")
    val friendListRouter = system.actorOf(Props(new FriendListRouter(15,FriendListMap,loadMonitorserivce)), name ="friendListServiceRouter")
    val photoServiceRouter = system.actorOf(Props(new PhotoServiceRouter(15,photoURLMap,loadMonitorserivce)), name ="photoServiceRouter")

  }  
}
