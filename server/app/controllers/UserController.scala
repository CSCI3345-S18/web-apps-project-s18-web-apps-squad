package controllers

import javax.inject._
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import slick.jdbc.JdbcProfile
import slick.jdbc.JdbcCapabilities
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.ExecutionContext
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms._
import scala.concurrent.Future
import models.UserModel
import models.BoardModel
import models.Board
import models.Post

@Singleton
class UserController @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    mcc: MessagesControllerComponents) (implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) with HasDatabaseConfigProvider[JdbcProfile] {

  val newUserForm = Form(mapping(
      "email" -> nonEmptyText,
      "username" -> nonEmptyText,
      "password" -> nonEmptyText)(NewUser.apply)(NewUser.unapply))
      
  val loginForm = Form(mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText)(Login.apply)(Login.unapply))
  
  def homePage() = Action.async { implicit request =>
    val boardsFuture = BoardModel.allBoards(db)
    boardsFuture.map(boards => Ok(views.html.homePage(boards)))
  }
  
  def allUsers = Action.async { implicit request =>
    val usersFuture = UserModel.allUsers(db)
    val boardsFuture = BoardModel.allBoards(db)
    for { 
      boards <- boardsFuture
      users <- usersFuture
    } yield (Ok(views.html.loginPage(boards, users, loginForm, newUserForm)))
    //usersFuture.map(users => Ok(views.html.loginPage(users, loginForm, newUserForm)))
  }
  
  def addUser = Action.async { implicit request =>
    newUserForm.bindFromRequest().fold(
        formWithErrors => {
          val usersFuture = UserModel.allUsers(db)
          val boardsFuture = BoardModel.allBoards(db)
          for {
            users <- usersFuture
            boards <- boardsFuture
          } yield (BadRequest(views.html.loginPage(boards, users, loginForm, formWithErrors)))
        },
        newUser => {
          val addFuture = UserModel.addUser(newUser, db)
          addFuture.map { cnt =>
            if(cnt == 1) Redirect(routes.UserController.allUsers).flashing("message" -> "New user added.")
            else Redirect(routes.UserController.allUsers).flashing("error" -> "Failed to add user.")
          }
        })
  }
  
  def login = Action.async { implicit request =>
    loginForm.bindFromRequest().fold(
        formWithErrors => {
          val usersFuture = UserModel.allUsers(db)
          val boardsFuture = BoardModel.allBoards(db)
          for {
            users <- usersFuture
            boards <- boardsFuture
          } yield (BadRequest(views.html.loginPage(boards, users, formWithErrors, newUserForm)))
        },
        loginUser => {
          val loginFuture = UserModel.verifyUser(loginUser, db)
          loginFuture.map { success =>
            if(success == true) Redirect(routes.UserController.userPage(loginUser.username)).withSession("connected" -> loginUser.username)
            else Redirect(routes.UserController.allUsers).flashing("error" -> "Failed to login.")
          }
        })
  }
  
  def logout = Action { implicit request =>
    Redirect(routes.UserController.allUsers).withNewSession
  }
  
  def userPage(username: String) = Action { implicit request =>
    Ok(views.html.userPage(username))
  }
  
  def loginPage() = Action.async { implicit request =>
    val usersFuture = UserModel.allUsers(db)
    val boardsFuture = BoardModel.allBoards(db)
    for {
      users <- usersFuture
      boards <- boardsFuture
    } yield(Ok(views.html.loginPage(boards, users, loginForm, newUserForm)))
  }

}