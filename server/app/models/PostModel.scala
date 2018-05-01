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
  /*def getPostsFromBoard(boardID: Int, db: Database): Future[Seq[Post]] = {
    // Pull all posts from the given board
  }*/

  def getPostFromPostID(postID: Int, db: Database)(implicit ec: ExecutionContext): Future[Option[Post]] = {
    db.run{
      posts.filter(_.id === postID).result.headOption
    }
  }

  def getCommentsFromPost(postID: Int): Seq[(String, String)] = {
    // Pull all comments from the particular post
    return Seq(("todo comment body", "todo commenter"))//feel free to make this a case class
  }

  def addCommentToPost() = {
    // Locate the post and then add comment
  }

  def addPost(np: NewPost, db: Database): Future[Int] = {
    db.run {
      //first number is an id which the database will ignore and last two are upvotes and downvotes
      posts += Post(0, np.boardID, np.posterID, np.title, np.body, np.link, 0, 0)
    }
  }
}
