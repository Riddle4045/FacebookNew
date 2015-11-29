package facebookClient

import common.Constants
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import facebookClient.clientService.ClientService
import akka.actor.Props
import common.StartClientRequests

object Client {

  def main(args : Array[String]){
        
        val numberOfUsers : Int = 1000    //change it to system paramters
       // val requestPerSecond : Int = 1000
        //parameters to control scaling factors
        val getRequestRate : Int = 1000;
        val postRequestRate : Int = 1000;

        val  constants = new Constants()
        val localAddress: String = java.net.InetAddress.getLocalHost.getHostAddress()
        val configString = """akka {
        actor {
          provider = "akka.remote.RemoteActorRefProvider"
        }
        remote {
          enabled-transports = ["akka.remote.netty.tcp"]
          netty.tcp {
            hostname = """ + localAddress + """
            port = """ + constants.SPRAY_CLIENT_PORT + """
          }
       }
      }"""
    
    val configuration = ConfigFactory.parseString(configString)
    implicit val system = ActorSystem("SprayClient", ConfigFactory.load(configuration))
    
    var clientServiceRouter = system.actorOf(Props(new ClientService(numberOfUsers,getRequestRate,postRequestRate,system)), name="clientServiceRouter") ! StartClientRequests
  }
  
}