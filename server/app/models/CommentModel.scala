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
import controllers.NewComment

object CommentModel {
  import Tables._

  def getCommentsFromPost(postParentID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Comment]] = {
    db.run {
      comments.filter(_.postParentID === postParentID).result
    }
  }
  def getCommentsFromUser(userID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Comment]] = {
    db.run {
      comments.filter(_.userID === userID).result
    }
  }
  def addComment(body: String, userID: Int, username: String, postParentID: Int, flag: Char, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      //0 for id upvotes and downvotes
      comments += Comment(0, body, userID, username, postParentID, flag, 0)
    }
  }
}
