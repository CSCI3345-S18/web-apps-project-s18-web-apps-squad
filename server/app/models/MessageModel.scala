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
import controllers.NewMessage

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

  def getMessagesToUserFromUser(receiverID: Int, senderID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Message]] = {
    db.run {
      messages.filter(_.receiverID === receiverID).filter(_.senderID === senderID).result
    }
  }

  def addMessage(m: NewMessage, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      messages += Message(0, m.body, m.senderID, m.receiverID)
    }
  }
}
