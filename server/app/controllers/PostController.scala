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

  val searchForm = Form(mapping(
      "query" -> nonEmptyText)(SearchQuery.apply)(SearchQuery.unapply))

  def addPost(boardTitle: String) = Action.async { implicit request =>
    request.session.get("connected").map { user =>
      val boardFutOpt = BoardModel.getBoardByTitle(boardTitle, db)
      val posterFutOpt = UserModel.getUserFromUsername(user, db)
      newPostForm.bindFromRequest().fold(
          formWithErrors => {
            boardFutOpt map { boardOption =>
              boardOption map { board =>
                BadRequest(views.html.addPostPage(boardTitle, formWithErrors, searchForm))
              } getOrElse {
                BadRequest(views.html.addPostPage("Bad board", formWithErrors, searchForm))
              }
            }
          },
          newPost => {
            boardFutOpt flatMap {
              case Some(board) => {
                posterFutOpt flatMap {
                  case Some(poster) => {
                    val addFuture = PostModel.addPost(board.id, poster.id, newPost, db)
                    addFuture flatMap { cnt =>
                      if(cnt == 1) Future(Redirect(routes.BoardController.boardPage(boardTitle))) // posted
                      else Future(Ok("Post not added"))
                    }
                  }
                  //No poster
                  case None => Future(Ok("Post not added because no poster"))
                }
              }
              //No Board
              case None => Future(Ok("Post not added because no board"))  
            }
          })
    }.getOrElse {
      val boards = BoardModel.allBoards(db)
      boards.map(boards => Redirect(routes.UserController.loginPage))
    }
  }
  
  def addPostPage(boardTitle: String) = Action { implicit request =>
    request.session.get("connected").map { user =>
      Ok(views.html.addPostPage(boardTitle, newPostForm, searchForm))
    }.getOrElse {
      Redirect(routes.UserController.loginPage)
    }
  }
  
  // Shows the original post inputs and loads its comments
  def postPage(title: String) = Action.async { implicit request =>
    val postFutOpt = PostModel.getPostFromTitle(title, db)
    postFutOpt.map {
      case Some(ogPost) =>
        Ok(views.html.postPage(ogPost.title, ogPost.body, ogPost.link, searchForm))
      case None =>
        Redirect(routes.UserController.homePage)
    }
  }
}
