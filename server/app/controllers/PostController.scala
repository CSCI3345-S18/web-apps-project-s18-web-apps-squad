package controllers

import javax.inject.Inject
import javax.inject.Singleton
import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.JdbcCapabilities
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util._
import models._

import javax.inject._
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api._
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

case class NewPost(title: String, body: String, link: String)

@Singleton
class PostController @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    mcc: MessagesControllerComponents) (implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) with HasDatabaseConfigProvider[JdbcProfile] {

  val newPostForm = Form(mapping(
      "title" -> nonEmptyText,
      "body" -> nonEmptyText,
      "link" -> nonEmptyText)(NewPost.apply)(NewPost.unapply))

  def addPost(boardTitle: String) = Action.async { implicit request =>
    val boardFuture = BoardModel.getBoardIDByTitle(boardTitle, db)
    val posterUsername = request.session.get("connected")
    val posterFuture = UserModel.getUserFromUsername(posterUsername.toString, db)
    newPostForm.bindFromRequest().fold(
        formWithErrors => {
          boardFuture map { boardOption =>
            boardOption map { board =>
              BadRequest(views.html.addPostPage(board.title, formWithErrors))
            } getOrElse {
              BadRequest(views.html.addPostPage("bad board", formWithErrors))
            }
          }
        },
        newPost => {
          boardFuture flatMap {
            case Some(board) =>{
              posterFuture flatMap {
                case Some(poster) => {
                  val addFuture = PostModel.addPost(board.id, poster.id, newPost, db)
                  addFuture flatMap { cnt =>
                    if(cnt == 1) Future(Ok("...")) // posted
                    else Future(Ok("..."))
                  }
                }
                //No poster
                case None => Future(Ok("..."))
              }
            }
            //No Board
            case None => Future(Ok("..."))
          }
        })
  }
  
  def addPostPage(boardTitle: String) = Action { implicit request =>
    Ok(views.html.addPostPage(boardTitle, newPostForm))
  }
}
