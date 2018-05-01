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

object PostModel {
  /*def getPostsFromBoard(boardID: Int, db: Database): Future[Seq[Post]] = {
    // Pull all posts from the given board
  }*/
  
  def getPostFromPostID(boardName: String, postID: Int): Option[Post] = {
    return Some(Post(postID, boardName, "TODO in DBData val postName", "TODO in DBData val postBody", "TODO in DBData val poster"))
  }
  
  def getCommentsFromPost(postID: Int): Seq[(String, String)] = {
    // Pull all comments from the particular post
    return Seq(("todo comment body", "todo commenter"))//feel free to make this a case class
  }  
  
  def addCommentToPost() = {
    // Locate the post and then add comment
  }
  
  def addPostToBoard() = {
    // Save post to something
    // Need a board to place post on
  }
}