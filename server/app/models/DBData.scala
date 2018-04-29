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

case class Post(id: Int, boardName: String, title: String, body: String, poster: String)
case class User(username: String, password: String, email: String)
case class Board(id: Int, title: String, description: String)
case class Comment(id: Int, flag: Char, postParentID: Int, commentParentID: Int)
case class Messages(id: Int, senderID: Int, receiverID: Int, messages: String)
case class Subscription(id: Int, userID: Int, boardID: Int)

object UserQueries {
  import Tables._
  
  def allUsers(db: Database)(implicit ec: ExecutionContext): Future[Seq[User]] = {
    db.run(users.result)
  }
  
  def addUser(nu: NewUser, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      users += User(nu.username, nu.password, nu.email)
    }
  }
  
  def verifyUser(lu: Login, db: Database)(implicit ec: ExecutionContext): Future[Boolean] = {
    db.run {
      users.filter(_.username === lu.username).filter(_.password === lu.password).exists.result
    }
  }

}

object SubQueries {
  import Tables._
}

class DBData() {
  
  def getPostsFromBoard(boardID: Int) = {
    // Pull all posts from the given board
  }
  
  def getSubscriptionsFromUser(username: String): Seq[String] = {
    return Seq("todo", "todo", "todo")
    // Pull all subscriptions from the user
  }
  
  def getDefaultSubscription(): Seq[String] = {
    return Seq("todo")
  }
  
  def getPostFromPostID(boardName: String, postID: Int): Option[Post] = {
    return Some(Post(postID, boardName, "TODO in DBData val postName", "TODO in DBData val postBody", "TODO in DBData val poster"))
  }
  
  def getCommentsFromPost(postID: Int): Seq[(String, String)] = {
    // Pull all comments from the particular post
    return Seq(("todo comment body", "todo commenter"))//feel free to make this a case class
  }
  
  def addUser(userID: Int, username: String, password: String, email: String) = {
    // Add a new user to the website
  }
  
  def verifyUser(username: String, password: String) = {
    
  }
  
  def addSubscription(userID: Int, subID: Int) = {
    // Add a new subscription to a user
  }
  
  def addCommentToPost() = {
    // Store comment into a variable filled by user
    // Need a post to send comment to
  }
  
  def addPostToBoard() = {
    // Save post to something
    // Need a board to place post on
  }

}