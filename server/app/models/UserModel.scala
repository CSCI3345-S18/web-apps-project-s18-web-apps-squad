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

  def getSubsFromUser(username: String): Seq[Board] = {
    return Seq()//TODO
  }

  def getUserFromID(id: Int, db: Database): Future[Option[User]] = {
    db.run {
      users.filter(_.id === id).result.headOption
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
