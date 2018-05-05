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
import models.PostModel
import models.Board
import models.Post
import models.CommentModel

case class NewComment(
    body: String
)

/*case class NewComment(
  body: String,
  userID: Int,
  postParentID: Int,
  commentParentID: Int,
  flag: Char
)*/

@Singleton
class CommentController @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    mcc: MessagesControllerComponents) (implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) with HasDatabaseConfigProvider[JdbcProfile] {
  
  val commentForm = Form(mapping(
      "body" -> nonEmptyText)(NewComment.apply)(NewComment.unapply))
  
  def addComment(title: String) = Action.async { implicit request =>
    request.session.get("connected").map { user =>
      val loggedinUser = UserModel.getUserFromUsername(user, db)
      commentForm.bindFromRequest().fold(
          formWithErrors => {
            loggedinUser.flatMap {
              case Some(actualUser) =>
                val subscribedBoardsFut = UserModel.getSubscriptionsOfUser(actualUser.id, db)
                for {
                  subs <- subscribedBoardsFut
                } yield {
                  Redirect(routes.PostController.postPage(title))
                }
              case None =>
                Future.successful(Redirect(routes.UserController.loginPage))
            }
          },
          newComment => {
            loggedinUser.flatMap {
              case Some(actualUser) =>
                val postFutOpt = PostModel.getPostFromTitle(title, db)
                postFutOpt.flatMap {
                  case Some(post) =>
                    val addFuture = CommentModel.addComment(newComment.body, actualUser.id, post.id, 1, 'F', db)
                    val commentsFutSeq = CommentModel.getCommentsFromPost(post.id, db)
                    val subsOfUser = UserModel.getSubscriptionsOfUser(actualUser.id, db)
                    addFuture.map { cnt =>
                      if(cnt == 1) Redirect(routes.PostController.postPage(title))
                      else Redirect(routes.PostController.postPage(title))
                    }
                  case None =>
                    Future.successful(Redirect(routes.PostController.postPage(title)))
                }
              case None =>
                Future.successful(Redirect(routes.UserController.loginPage))
            }
          })
    }.getOrElse {
      Future.successful(Redirect(routes.UserController.loginPage))
    }
  }

}
