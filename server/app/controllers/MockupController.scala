package controllers

/*import javax.inject.Inject
import javax.inject.Singleton
import play.api.mvc.MessagesAbstractController
import play.api.mvc.MessagesControllerComponents*/

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
import models.UserQueries

case class NewUser(email: String, username: String, password: String)
case class Login(username: String, password: String)

@Singleton
class MockupController @Inject() (
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
  
  def homePage() = Action { implicit request =>
    Ok(views.html.homePage())
  }
  
  def allUsers = Action.async { implicit request =>
    val usersFuture = UserQueries.allUsers(db)
    usersFuture.map(users => Ok(views.html.loginPage(users, loginForm, newUserForm)))
  }
  
  def addUser = Action.async { implicit request =>
    newUserForm.bindFromRequest().fold(
        formWithErrors => {
          val usersFuture = UserQueries.allUsers(db)
          usersFuture.map(users => BadRequest(views.html.loginPage(users, loginForm, formWithErrors)))
        },
        newUser => {
          val addFuture = UserQueries.addUser(newUser, db)
          addFuture.map { cnt =>
            if(cnt == 1) Redirect(routes.MockupController.allUsers).flashing("message" -> "New user added.")
            else Redirect(routes.MockupController.allUsers).flashing("error" -> "Failed to add user.")
          }
        })
  }
  
  def login = Action.async { implicit request =>
    loginForm.bindFromRequest().fold(
        formWithErrors => {
          val usersFuture = UserQueries.allUsers(db)
          usersFuture.map(users => BadRequest(views.html.loginPage(users, formWithErrors, newUserForm)))
        },
        loginUser => {
          val loginFuture = UserQueries.verifyUser(loginUser, db)
          loginFuture.map { success =>
            //if(success == true) Redirect(routes.MockupController.getSubs(loginUser.username))
            if(success == true) Redirect(routes.MockupController.allUsers).flashing("message" -> "Login success.")
            else Redirect(routes.MockupController.allUsers).flashing("error" -> "Failed to login.")
          }
        })
  }
  
  /*def getSubs(username: String) = Action.async { implicit requeset =>
    
  }*/
  
  def userPage() = Action { implicit request =>
    Ok(views.html.userPage())
  }
  
  def postPage() = Action { implicit request =>
    Ok("")//Ok(views.html.postPage())
  }
  
  
  
}