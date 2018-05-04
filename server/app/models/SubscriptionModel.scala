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
import controllers.NewSubscription



object SubscriptionModel {
  import Tables._

  def getSubscriptionFromBoard(boardID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Subscription]] = {
    db.run {
      subscriptions.filter(_.boardID === boardID).result
    }
  }
  def getSubscriptionFromUser(userID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Subscription]] = {
    db.run {
      subscriptions.filter(_.userID === userID).result
    }
  }
  def addComment(s: NewSubscription, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      //0 for id
      subscriptions += Subscription(0, s.userID, s.boardID, s.title)
    }
  }
}
