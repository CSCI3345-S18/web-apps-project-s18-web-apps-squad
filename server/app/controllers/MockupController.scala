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

case class NewUser(email: String, username: String, password: String)
case class Login(username: String, password: String)

case class NewPost(boardID: Int, posterID: Int, title: String, body: String, link: String)
case class NewBoard(title: String, description: String) 

@Singleton
class MockupController @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    mcc: MessagesControllerComponents) (implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) with HasDatabaseConfigProvider[JdbcProfile] {

  /*val newUserForm = Form(mapping(
      "email" -> nonEmptyText,
      "username" -> nonEmptyText,
      "password" -> nonEmptyText)(NewUser.apply)(NewUser.unapply))

  val loginForm = Form(mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText)(Login.apply)(Login.unapply))

  val newBoardForm = Form(mapping(
      "title" -> nonEmptyText,
      "description" -> nonEmptyText)(Board.apply)(Board.unapply))

  def homePage() = Action { implicit request =>
    Ok(views.html.homePage())
  }

  def allUsers = Action.async { implicit request =>
    val usersFuture = UserModel.allUsers(db)
    usersFuture.map(users => Ok(views.html.loginPage(users, loginForm, newUserForm)))
  }

  def addUser = Action.async { implicit request =>
    newUserForm.bindFromRequest().fold(
        formWithErrors => {
          val usersFuture = UserModel.allUsers(db)
          usersFuture.map(users => BadRequest(views.html.loginPage(users, loginForm, formWithErrors)))
        },
        newUser => {
          val addFuture = UserModel.addUser(newUser, db)
          addFuture.map { cnt =>
            if(cnt == 1) Redirect(routes.MockupController.allUsers).flashing("message" -> "New user added.")
            else Redirect(routes.MockupController.allUsers).flashing("error" -> "Failed to add user.")
          }
        })
  }

  def allBoards = Action.async { implicit request =>
    val boardsFuture = BoardModel.allBoards(db)
    boardsFuture.map(boards => Ok(views.html.addBoardPage(boards, newBoardForm)))
  }

  def addBoard = Action.async { implicit request =>
    newBoardForm.bindFromRequest().fold(
        formWithErrors => {
          val boardsFuture = BoardModel.allBoards(db)
          boardsFuture.map(boards => BadRequest(views.html.addBoardPage(boards, newBoardForm)))
        },
        newBoard => {
          val addFuture = BoardModel.addBoard(newBoard, db)
          addFuture.map { cnt =>
            //if(cnt == 1) Redirect(routes.MockupController.allBoards).flashing("message" -> "New board added.")
            if(cnt == 1) Redirect(routes.MockupController.boardPage(newBoard.title, newBoard.description))
            else Redirect(routes.MockupController.allBoards).flashing("error" -> "Failed to add user.")
          }
        })
  }

  def login = Action.async { implicit request =>
    loginForm.bindFromRequest().fold(
        formWithErrors => {
          val usersFuture = UserModel.allUsers(db)
          usersFuture.map(users => BadRequest(views.html.loginPage(users, formWithErrors, newUserForm)))
        },
        loginUser => {
          val loginFuture = UserModel.verifyUser(loginUser, db)
          loginFuture.map { success =>
            //if(success == true) Redirect(routes.MockupController.getSubs(loginUser.username))
            if(success == true) Redirect(routes.MockupController.userPage(loginUser.username)).withSession("connected" -> loginUser.username)
            else Redirect(routes.MockupController.allUsers).flashing("error" -> "Failed to login.")
          }
        })
  }

  def logout = Action { implicit request =>
    Redirect(routes.MockupController.allUsers).withNewSession
  }

  /*def getSubs(username: String) = Action.async { implicit requeset =>
      val subsFuture = SubQueries.getTasks(username, db)
      subsFuture.map(subs => Ok(views.html.profilePage(username, subs)))
  }*/

  def userPage(username: String) = Action { implicit request =>
    Ok(views.html.userPage(username))
  }

  def postPage() = Action { implicit request =>
    Ok("")//Ok(views.html.postPage())
  }

  def loginPage() = Action.async { implicit request =>
    val usersFuture = UserModel.allUsers(db)
    usersFuture.map(users => Ok(views.html.loginPage(users, loginForm, newUserForm)))
  }

  def boardPage(title: String, desc: String) = Action.async { implicit request =>
    val boardsFuture = BoardModel.allBoards(db)
    boardsFuture.map(boards => Ok(views.html.boardPage(title, desc)))
  }*/

}
