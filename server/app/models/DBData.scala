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

case class Post(id: Int, boardID: Int, posterID: Int, title: String, body: String)
case class User(id: Int, username: String, password: String, email: String)
case class Board(id: Int, title: String, description: String)
case class Comment(id: Int, flag: Char, postParentID: Int, commentParentID: Int)
case class Messages(id: Int, senderID: Int, receiverID: Int, messages: String)
case class Subscription(id: Int, userID: Int, boardID: Int)

class DBData() {
  
  def getPostsFromBoard(boardID: Int) = {
    // Pull all posts from the given board
  }
  
  def getSubscriptionsFromUser(userID: Int) = {
    // Pull all subscriptions from the user
  }
  
  def getCommentsFromPost(postID: Int) = {
    // Pull all comments from the particular post
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