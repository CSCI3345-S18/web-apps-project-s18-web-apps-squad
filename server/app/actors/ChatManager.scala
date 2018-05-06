package actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

class ChatManager extends Actor {
  import ChatManager._
  
  private var actors = scala.collection.mutable.Map[(Int, Int), ActorRef]()
  
  def receive = {
    case NewUser(uOne, uTwo, handler) => actors((uOne, uTwo)) = handler
  }
}

object ChatManager {
  def props = Props[ChatManager]
  
  case class NewUser(senderID: Int, receiverID: Int, handler: ActorRef)
}