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

object UserModel {
  import Tables._
  
  def getFriendsipsOfUser(userID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Friendship]] = {
    db.run{
      friends.filter(friendship => friendship.userOneID === userID || friendship.userTwoID === userID).result
    }
  }
  
  def getCommentsOfUser(userID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Comment]] = {
    db.run {
      comments.filter(_.userID === userID).result
    }
  }
  
  def getPostsOfUser(userID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Post]] = {
    db.run {
      posts.filter(_.posterID === userID).result
    }
  }
  
  def checkIfUsernameExists(username: String, db: Database)(implicit ec: ExecutionContext): Future[Boolean] = {
    db.run {
      users.filter(_.username === username).exists.result
    }
  }
  
  def getSubscriptionsOfUser(userID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Subscription]] = {
    db.run {
      subscriptions.filter(_.userID === userID).result
    }
  }
  
  def getUserFromID(id: Int, db: Database)(implicit ec: ExecutionContext): Future[Option[User]] = {
    db.run {
      users.filter(_.id === id).result.headOption
    }
  }
  
  def getUserFromUsername(username: String, db: Database)(implicit ec: ExecutionContext): Future[Option[User]] = {
    db.run {
      users.filter(_.username === username).result.headOption
    }
  }

  def allUsers(db: Database)(implicit ec: ExecutionContext): Future[Seq[User]] = {
    db.run(users.result)
  }

  def addUser(nu: NewUser, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      users += User(0, nu.username, nu.password, nu.email)
    }
  }

  def verifyUser(lu: Login, db: Database)(implicit ec: ExecutionContext): Future[Boolean] = {
    db.run {
      users.filter(_.username === lu.username).filter(_.password === lu.password).exists.result
    }
  }
  
  def searchUsersByUsername(username: String, db: Database)(implicit ec: ExecutionContext): Future[Seq[User]] = {
    db.run {
      users.filter(_.username like username+"%").result
    }
  }

}
