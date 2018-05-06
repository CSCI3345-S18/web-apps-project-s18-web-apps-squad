package models

import javax.inject.Inject
import javax.inject.Singleton
import play.api.data._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.data.Forms.number
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents
import scala.collection.mutable.Map
import scala.collection.mutable.Buffer

import slick.jdbc.MySQLProfile.api._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import actors.ChatManager.ActorMessage

object MessageModel {
  import Tables._
  
  def getMessagesToUser(receiverID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Message]] = {
    db.run {
      messages.filter(_.receiverID === receiverID).result
    }
  }

  def getMessagesFromUser(senderID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Message]] = {
    db.run {
      messages.filter(_.senderID === senderID).result
    }
  }

  def getMessagesBetweenFriends(userOneID: Int, userTwoID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Message]] = {
    db.run {
      messages.filter(m => (m.receiverID === userOneID && m.senderID === userTwoID) || (m.receiverID === userTwoID && m.senderID === userOneID)).result
    }
  }

  def addMessage(m: ActorMessage, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      messages += Message(0, m.body, m.senderID, m.receiverID)
    }
  }
  
  def areFriends(userOneID: Int, userTwoID: Int, db: Database)(implicit ec: ExecutionContext): Future[Boolean] = {
    db.run{
      friends.filter(f => (f.userOneID === userOneID && f.userTwoID === userTwoID) || (f.userOneID === userTwoID && f.userTwoID === userOneID)).exists.result
    }
  }
  
  def addFriends(userOneID: Int, userTwoID: Int, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run{
      friends += Friendship(0, userOneID, userTwoID)
    }
  }
}
