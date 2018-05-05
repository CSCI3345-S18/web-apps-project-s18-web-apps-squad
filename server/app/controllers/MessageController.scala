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

case class NewMessage(
  body: String,
  senderID: Int,
  receiverID: Int
)

@Singleton
class MessageController @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    mcc: MessagesControllerComponents) (implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) with HasDatabaseConfigProvider[JdbcProfile] {

    val searchForm = Form(mapping(
      "query" -> nonEmptyText)(SearchQuery.apply)(SearchQuery.unapply))  
  
    def messagesPage() = Action.async { implicit request =>
      request.session.get("connected").map { user =>
        val loggedinUser = UserModel.getUserFromUsername(user, db)
        loggedinUser.flatMap {
          case Some(actualUser) => {
            val boardsFuture = UserModel.getSubscriptionsOfUser(actualUser.id, db)
            boardsFuture.flatMap{boards =>
              val friendshipsFuture = UserModel.getFriendsipsOfUser(actualUser.id, db)
              friendshipsFuture.flatMap{friends =>
                val friendIDs = friends.map{ friend =>
                  if(friend.userOneID != actualUser.id)
                    friend.userOneID
                  else
                    friend.userTwoID
                }
                val friendUsernameFutures = Future.sequence(friendIDs.map{UserModel.getUserFromID(_, db)})
              
                for{
                  maybeUsers <- friendUsernameFutures
                } yield {
                  val friendUsernames = maybeUsers.collect{case Some(friend) => friend.username}
                  Ok(views.html.messagesPage(boards, searchForm, friendUsernames, friendIDs))
                }
              }
            }
          }
          case None =>
            val usersFuture = UserModel.allUsers(db)
            usersFuture.map(users => Redirect(routes.UserController.loginPage))
        }
      }.getOrElse {
        val boardsFuture = BoardModel.allBoards(db)
        boardsFuture.map(boards => Redirect(routes.UserController.loginPage))
      }
  }
    
  def messagingPage(friend: String) = Action.async { implicit request =>
    val res = "you are messaging" + friend
    Future(Ok(res))
  }
}
