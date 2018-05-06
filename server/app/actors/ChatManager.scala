package actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import models.MessageModel
import controllers.MessageController

class ChatManager(mc: MessageController) extends Actor {
  import ChatManager._
  
  private var actors = scala.collection.mutable.Map[(Int, Int), ActorRef]()
  
  def receive = {
    case NewActor(uOne, uTwo, handler) => {
      actors((uOne, uTwo)) = handler
    }
    case ActorMessage(msg, uOne, uTwo) => {
      mc.addMessage(ActorMessage(msg, uOne, uTwo))
      if(actors.contains((uTwo, uOne)))
        actors((uTwo, uOne)) ! ActorMessage(msg, uOne, uTwo)
    }
  }
}

object ChatManager {
  def props(mc: MessageController) = Props(new ChatManager(mc))
  
  case class NewActor(senderID: Int, receiverID: Int, handler: ActorRef)
  case class ActorMessage(body: String, senderID: Int, receiverID: Int)
}