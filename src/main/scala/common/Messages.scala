package common


  trait Messages 

  case class addPost(receiverId :String, senderId: String, post : String) extends Messages
  case class updateProfile(userId: String , profileField : String , profileValue : String) extends Messages
  case class updateWall(receiverId :String, senderId: String, post : String) extends Messages
  case class FinishedWork(typeOfWork :String) extends Messages
  case class GetProfile(userId : String) extends Messages
  case class GetTimeLine(userId : String) extends Messages
  case object StartClientRequests extends Messages
  case object SendRequestToServer extends Messages
  case object GetWall extends Messages
  case object PostToWall extends Messages
  case class PostToTimeLine(userId : String) extends Messages
  case class PostToProfile(userId : String) extends Messages
  case object PrintThreadCount extends Messages
  case class addFriends(fromId : String, toId: String) extends Messages
  case class addPhotos(fromId :  String, toId:String, url:String) extends Messages