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
