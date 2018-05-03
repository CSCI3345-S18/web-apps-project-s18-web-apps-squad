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

//case class NewUser(email: String, username: String, password: String)
//case class Login(username: String, password: String)

case class NewBoard(title: String, description: String)

@Singleton
class BoardController @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    mcc: MessagesControllerComponents) (implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) with HasDatabaseConfigProvider[JdbcProfile] {
      
  val newBoardForm = Form(mapping(
      "title" -> nonEmptyText,
      "description" -> nonEmptyText)(NewBoard.apply)(NewBoard.unapply))
  
  def allBoards = Action.async { implicit request =>
    val boardsFuture = BoardModel.allBoards(db)
    boardsFuture.map(boards => Ok(views.html.popularBoardsPage(boards)))
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
              if(cnt == 1) Redirect(routes.BoardController.boardPage(newBoard.title, newBoard.description))
              else Redirect(routes.BoardController.allBoards).flashing("error" -> "Failed to add user.")
          }
        })
  }
  
  def postPage() = Action { implicit request =>
    Ok("")//Ok(views.html.postPage())
  }
  
  def boardPage(title: String, desc: String) = Action.async { implicit request =>
    val boardsFuture = BoardModel.allBoards(db)
    boardsFuture.map(boards => Ok(views.html.boardPage(title, desc)))
  }
  
  def addBoardPage() = Action.async { implicit request =>
    val boardsFuture = BoardModel.allBoards(db)
    boardsFuture.map(boards => Ok(views.html.addBoardPage(boards, newBoardForm)))
  }

}