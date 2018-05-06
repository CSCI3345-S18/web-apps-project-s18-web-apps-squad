package actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

class ChatActor(out: ActorRef, manager: ActorRef) extends Actor {
  import ChatActor._
  import ChatManager._
  
  private var myID = -1
  private var friendsID = -1
  
  def receive = {
    case newClient(id, fId) => {
      myID = id.toInt
      friendsID = fId.toInt
      manager ! NewActor(myID.toInt, friendsID.toInt, self)
    }
    case str:String => manager ! ActorMessage(str, myID, friendsID)
    case ActorMessage(msg, uOne, uTwo) => {
      if(myID != uTwo && friendsID != uOne)
        println("[error] MESSAGE DID NOT GO TO THE RIGHT ACTOR" + uOne + ", " + uTwo)
      else
        out ! msg
    }
  }
}

object ChatActor {
  def props(out: ActorRef, manager: ActorRef) = Props(new ChatActor(out, manager))
  
  val newClient = raw"(\d), (\d)".r
}