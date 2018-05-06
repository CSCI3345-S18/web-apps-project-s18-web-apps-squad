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
import controllers.Login
import controllers.NewUser
import controllers.NewBoard

object BoardModel {
  import Tables._

  def allBoards(db: Database)(implicit ec: ExecutionContext): Future[Seq[Board]] = {
    db.run(boards.result)
  }

  def addBoard(b: NewBoard, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      boards += Board(0, b.title, b.description)
    }
  }
  def getTopBoardsForUser(userID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Board]] = {
    db.run {
      val b = subscriptions.filter(_.userID === userID).join(boards).on(_.boardID === _.id).map( b => b._2)
      b.join(subscriptions).groupBy(_._1).map { case (bor, subs) =>
        (bor, subs.map(_._2).length)} sortBy(_._2) take(5) map(_._1) result
    }
  }
  def getTopBoards(db: Database)(implicit ec: ExecutionContext): Future[Seq[Board]] = {
    db.run {
      boards.join(subscriptions).groupBy(_._1).map { case (bor, subs) =>
        (bor, subs.map(_._2).length)} sortBy(_._2) take(5) map(_._1) result
    }
  }
  def getSubscriptionNum(boardID: Int, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      subscriptions.filter(_.boardID === boardID).length.result
    }
  }
  def getBottomBoards(db: Database)(implicit ec: ExecutionContext): Future[Seq[Board]] = {
    db.run {
      boards.join(subscriptions).groupBy(_._1).map { case (bor, subs) =>
        (bor, subs.map(_._2).length)} sortBy(_._2.desc) take(5) map(_._1) result
    }
  }
  def getDefaultSubscription(): Seq[String] = {
    return Seq("todo")
  }

  def searchBoardsByTitle(title: String, db: Database)(implicit ec: ExecutionContext): Future[Seq[Board]] = {
    db.run {
      boards.filter(_.title like title+"%").result
    }
  }

  def getBoardByID(boardID: Int, db: Database)(implicit ec: ExecutionContext): Future[Option[Board]] = {
    db.run {
      boards.filter(_.id === boardID).result.headOption
    }
  }

  def getBoardByTitle(title: String, db: Database)(implicit ec: ExecutionContext): Future[Option[Board]] = {
    db.run {
      boards.filter(_.title === title).result.headOption
    }
  }

}
