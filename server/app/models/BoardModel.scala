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
    val tmpID = 0;
    db.run {
      boards += Board(tmpID, b.title, b.description)
    }
  }
  
  def getDefaultSubscription(): Seq[String] = {
    return Seq("todo")
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
  
  def getTitle(b: Board, db: Database)(implicit ec: ExecutionContext): String = {
    return b.title
  }
  
  /*def getPostsFromBoard(boardID: Int, db: Database): Future[Seq[Post]] = {
    
  }*/
  
  /*def getCommentsFromPost(boardName: String, postID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Comment]] = {
    
  }*/
  
  /*def addSubscriptionToUser(username: String, subID: Int): Future[Int] = {
    
  }*/
  
  /*def addCoommentToPost(postID: Int): Future[Int] = {
    
  }*/
  
  /*def addPostToBoard(boardID: Int): Future[Int] = {
    
  }*/
  
}