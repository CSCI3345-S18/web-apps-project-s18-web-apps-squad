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
import models.CommentModel
import models.SubscriptionModel
import controllers.util.checkLogin

case class NewSubscription(
  userID: Int,
  boardID: Int,
  title: String
)

@Singleton
class SubscriptionController @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    mcc: MessagesControllerComponents) (implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) with HasDatabaseConfigProvider[JdbcProfile] {

  def addSubscription(title: String) = Action.async { implicit request =>
    request.session.get("connected").map { user =>
      val loggedinUser = UserModel.getUserFromUsername(user, db)
      val boardToSubscribeTo = BoardModel.getBoardByTitle(title, db)
      loggedinUser.flatMap {
        case Some(user) =>
          boardToSubscribeTo.flatMap {
            case Some(board) =>
              val checkSubscription = SubscriptionModel.checkIfSubscribed(user.id, board.id, db)
              checkSubscription.map(exists =>
                if(!exists) {
                  SubscriptionModel.addSubscription(user.id, board.id, board.title, db)
                  Redirect(routes.BoardController.boardPage(board.title))
                } else {
                  Redirect(routes.BoardController.boardPage(board.title))
                })
            case None =>
              Future.successful(Redirect(routes.UserController.homePage))
          }
        case None =>
          Future.successful(Redirect(routes.UserController.homePage))
      }
    }.getOrElse {
      Future.successful(Redirect(routes.UserController.loginPage))
    }
  }
  def deleteSubscription(userID: Int, boardID: Int) = Action.async { implicit request =>
    checkLogin(request, db).flatMap{
      case Some(user) =>
        val subFut = SubscriptionModel.deleteSubscription(userID, boardID, db)
        for {
           sub <- subFut
        } yield {
          Redirect(routes.UserController.homePage)
        }
      case None => Future.successful(Redirect(routes.UserController.loginPage))
    }
  }
}
