package actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

class ChatActor(out: ActorRef, manager: ActorRef) extends Actor {
  def receive = {
    case _ => ()
  }
}

object ChatActor {
  def props(out: ActorRef, manager: ActorRef) = Props(new ChatActor(out, manager))
  
  
}