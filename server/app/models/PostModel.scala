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
import controllers.NewPost

object PostModel {
  import Tables._
  
  def getPostsFromBoard(boardID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Post]] = {
    db.run {
      posts.filter(_.boardID === boardID).result
    }
  }

  def getPostFromID(postID: Int, db: Database)(implicit ec: ExecutionContext): Future[Option[Post]] = {
    db.run{
      posts.filter(_.id === postID).result.headOption
    }
  }
  
  def getPostFromTitle(title: String, db: Database)(implicit ec: ExecutionContext): Future[Option[Post]] = {
    db.run {
      posts.filter(_.title === title).result.headOption
    }
  }

  def getCommentsFromPost(postID: Int): Seq[(String, String)] = {
    // Pull all comments from the particular post
    return Seq(("todo comment body", "todo commenter"))//feel free to make this a case class
  }

  def addCommentToPost() = {
    // Locate the post and then add comment
  }

  def addPost(boardID: Int, posterID: Int, np: NewPost, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      //first number is an id which the database will ignore and last two are upvotes and downvotes which database should ignore
      posts += Post(0, boardID, posterID, np.title, np.body, np.link, 0, 0)
    }
  }
  def searchPostsByTitle(title: String, db: Database)(implicit ec: ExecutionContext): Future[Seq[Post]] = {
    db.run {
      posts.filter(_.title like title+"%").result
    }
  }
}
