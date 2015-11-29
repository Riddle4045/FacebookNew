package facebookServer.services

import akka.actor.Actor
import common.FinishedWork
import java.util.Calendar
import akka.actor.ActorSystem
import common.PrintThreadCount
import scala.concurrent.duration._
import akka.util.Timeout
import akka.actor.ActorRef


class LoadMonitorService(actorsystem : ActorSystem) extends Actor{
  
 // val startTime = System.currentTimeMillis()
//  var threadsPerSecond : Float= 0;
  var postThreads = 0;
  var profileThreads = 0;
  var wallThreads = 0;
  var getThreads=0;
  var allThreads : Long= 0;
  var addFriendThreads  =0;
  var photoThreads = 0;
  
  implicit val system = actorsystem
  implicit val timeout: Timeout = Timeout(15.seconds)
  import system.dispatcher
  
system.scheduler.schedule(0 milli, 1000 milli, self.asInstanceOf[ActorRef], PrintThreadCount)
 
  def incrementThreadCounts(typeOfWork :  String){
     typeOfWork match {
       case "addPost" =>{ postThreads = postThreads +1; allThreads = allThreads +1 }
       case "updateProfile" =>{ profileThreads = profileThreads +1; allThreads = allThreads + 1}
       case "updateWall" => {wallThreads = wallThreads+1; allThreads = allThreads +1 }
       case "getCall" => { getThreads = getThreads +1 ;  allThreads = allThreads +1}
       case "addFriend" => { addFriendThreads = addFriendThreads + 1 ; allThreads = allThreads + 1}
       case "postPhoto" => { photoThreads = photoThreads  +1 ; allThreads = allThreads +1}
       case _ => println("Invalid Type of Thread");
     }
     //var endTime = System.currentTimeMillis()
     //var timeDifference = (endTime - startTime)
   //  threadsPerSecond = allThreads.toFloat/timeDifference
  //   println(threadsPerSecond*1000)
  }
  
  def receive = {
    
    case FinishedWork(typeOfWork :String) => incrementThreadCounts(typeOfWork)
    case PrintThreadCount =>
      println(allThreads)
      allThreads = 0;
    
  }
  
  
  
  
}