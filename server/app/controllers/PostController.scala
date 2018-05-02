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

case class newPost(title: String, body: String, link: String)

@Singleton
class PostController @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    mcc: MessagesControllerComponents) (implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) with HasDatabaseConfigProvider[JdbcProfile] {

  def viewPost(board: String, postIDStr: String) = Action.async { implicit request =>
    val boards: Seq[String] = request.session.get("username").map{username =>
					UserModel.getSubsFromUser(username).map(_.toString())
				}.getOrElse{
					BoardModel.getDefaultSubscription()
				}
    val postID = postIDStr.toInt
    val postOptFuture = PostModel.getPostFromPostID(postID, db)
    postOptFuture.map(postOpt =>
      postOpt match {
        case None => Ok(views.html.errorPage("Post not found", boards))
        case Some(post) => {
          val comments = PostModel.getCommentsFromPost(postID)
          val userOptFuture = UserModel.getUserFromID(post.posterID, db)
          val userOptTry = Await.ready(userOptFuture, Duration.Inf).value.get
          userOptTry match {
            case Success(userOpt) => {
              userOpt match {
                case None => Ok(views.html.errorPage("User not found", boards))
                case Some(user) => {
                  Ok(views.html.postPage(board, boards, post.title, post.body, user.username, comments))
                }
              }
            }
            case Failure(e) => Ok(views.html.errorPage("User not found", boards))

          }
        }
      }
  )}
  
  val newPostForm = Form(mapping(
      "title" -> nonEmptyText,
      "body" -> nonEmptyText,
      "link" -> nonEmptyText)(newPost.apply)(newPost.unapply))
  
  /*def addPost = Action.async { implicit request =>
      newPostForm.bindFromRequeset().fold(
          formWithErrors => {
            val postsFuture = PostModel.
          }
  }*/
}
