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
case class NewVotePost(
  postID: Int,
  userID: Int,
  upvote: Boolean
)
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

  val commentForm = Form(mapping(
      "body" -> nonEmptyText)(NewComment.apply)(NewComment.unapply))

  def addPost(boardTitle: String) = Action.async { implicit request =>
    request.session.get("connected").map { user =>
      val loggedinUser = UserModel.getUserFromUsername(user, db)
      newPostForm.bindFromRequest().fold(
          formWithErrors => {
            loggedinUser.flatMap {
              case Some(actualUser) =>
                val subbedBoards = UserModel.getSubscriptionsOfUser(actualUser.id, db)
                subbedBoards.map(subs => BadRequest(views.html.addPostPage(subs, boardTitle, formWithErrors, searchForm)))
              case None =>
                Future.successful(Redirect(routes.UserController.loginPage))
            }
          },
          newPost => {
            val addedPost = PostModel.getPostFromTitle(boardTitle, db)
            val boardBeingAddedTo = BoardModel.getBoardByTitle(boardTitle, db)
            loggedinUser.flatMap {
              case Some(actualUser) =>
                boardBeingAddedTo.flatMap {
                  case Some(board) =>
                    val addFuture = PostModel.addPost(board.id, actualUser.id, newPost, db)
                    addFuture.map { cnt =>
                      if(cnt == 1) Redirect(routes.BoardController.boardPage(boardTitle))
                      else Redirect(routes.BoardController.boardPage(boardTitle))
                    }
                  case None =>
                    Future(Ok("Please add to an actual board"))
                }
              case None =>
                Future.successful(Redirect(routes.UserController.loginPage))
            }
          })
    }.getOrElse {
      Future.successful(Redirect(routes.UserController.loginPage))
    }
  }

  def addPostPage(boardTitle: String) = Action.async { implicit request =>
    request.session.get("connected").map { user =>
      val loggedinUser = UserModel.getUserFromUsername(user, db)
      loggedinUser.flatMap {
        case Some(actualUser) =>
          val subbedBoardsSeq = UserModel.getSubscriptionsOfUser(actualUser.id, db)
          for {
            subs <- subbedBoardsSeq
          } yield {
            Ok(views.html.addPostPage(subs, boardTitle, newPostForm, searchForm))
          }
        case None =>
          Future.successful(Redirect(routes.UserController.loginPage))
      }
    }.getOrElse {
      Future.successful(Redirect(routes.UserController.loginPage))
    }
  }

  // Shows the original post inputs and loads its comments
  def postPage(title: String) = Action.async { implicit request =>
    request.session.get("connected").map { user =>
      val loggedinUser = UserModel.getUserFromUsername(user, db)
      loggedinUser.flatMap {
        case Some(actualUser) =>
          val postFutOpt = PostModel.getPostFromTitle(title, db)
          postFutOpt.flatMap {
            case Some(post) =>
              val commentsFutSeq = CommentModel.getCommentsFromPost(post.id, db)
              val subsOfUser = UserModel.getSubscriptionsOfUser(actualUser.id, db)
              for {
                comments <- commentsFutSeq
                subs <- subsOfUser
              } yield {
                Ok(views.html.postPage(subs, comments, post.title, post.body, post.link, searchForm, commentForm))
              }
            case None =>
              Future.successful(Redirect(routes.UserController.homePage))
          }
        case None =>
          val postFutOpt = PostModel.getPostFromTitle(title, db)
          postFutOpt.flatMap {
            case Some(post) =>
              val commentsFutSeq = CommentModel.getCommentsFromPost(post.id, db)
              val emptySubs: Seq[Subscription] = Seq()
              for {
                comments <- commentsFutSeq
              } yield {
                Ok(views.html.postPage(emptySubs, comments, post.title, post.body, post.link, searchForm, commentForm))
              }
            case None =>
              Future.successful(Redirect(routes.BoardController.allBoards))
          }
      }
    }.getOrElse {
      val postFutOpt = PostModel.getPostFromTitle(title, db)
      postFutOpt.flatMap {
        case Some(post) =>
          val commentsFutSeq = CommentModel.getCommentsFromPost(post.id, db)
          val emptySubs: Seq[Subscription] = Seq()
          for {
            comments <- commentsFutSeq
          } yield {
            Ok(views.html.postPage(emptySubs, comments, post.title, post.body, post.link, searchForm, commentForm))
          }
        case None =>
          Future.successful(Redirect(routes.BoardController.allBoards))
      }
    }
  }
}
