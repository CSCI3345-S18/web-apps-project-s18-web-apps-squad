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
  def getSubscriptionsFromUser(userID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Subscription]] = {
    db.run {
      subscriptions.filter(_.userID === userID).result
    }
  }
  def addSubscription(userID: Int, boardID: Int, title: String, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      //0 for id
      subscriptions += Subscription(0, userID, boardID, title)
    }
  }

  def deleteSubscription(userID: Int, boardID: Int, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      subscriptions.filter(_.userID === userID).filter(_.boardID === boardID).delete
    }
  }
  def checkIfSubscribed(userID: Int, boardID: Int, db: Database)(implicit ec: ExecutionContext): Future[Boolean] = {
    db.run {
      subscriptions.filter(_.boardID === boardID).filter(_.userID === userID).exists.result
    }
  }
}
