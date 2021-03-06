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
import models.Subscription

//case class NewUser(email: String, username: String, password: String)
//case class Login(username: String, password: String)
case class SearchQuery(query: String)

@Singleton
class SearchController @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    mcc: MessagesControllerComponents) (implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) with HasDatabaseConfigProvider[JdbcProfile] {
  
  val searchForm = Form(mapping(
      "search" -> text)(SearchQuery.apply)(SearchQuery.unapply))
  
  def searchPage = Action.async { implicit request =>
    request.session.get("connected").map { user =>
      searchForm.bindFromRequest().fold(
        formWithErrors => {
          val boardsFuture = BoardModel.allBoards(db)
          boardsFuture.map(boards => Redirect(routes.UserController.homePage))
        },
        searchQuery => {
          val loggedinUser = UserModel.getUserFromUsername(user, db)
          loggedinUser.flatMap {
            case Some(actualUser) =>
              val subbedBoardsFuture = UserModel.getSubscriptionsOfUser(actualUser.id, db)
              val searchedBoardsFuture = BoardModel.searchBoardsByTitle(searchQuery.query, db)
              val searchedPostsFuture = PostModel.searchPostsByTitle(searchQuery.query, db)
              val searchedUsersFuture = UserModel.searchUsersByUsername(searchQuery.query, db)
              for {
                subs <- subbedBoardsFuture
                searchedBoards <- searchedBoardsFuture
                searchedPosts <- searchedPostsFuture
                searchedUsers <- searchedUsersFuture
              } yield {
                Ok(views.html.searchPage(subs, searchedBoards, searchedPosts, searchedUsers, searchForm))
              }
            case None =>
              val emptySubs: Seq[Subscription] = Seq()
              val searchedBoardsFuture = BoardModel.searchBoardsByTitle(searchQuery.query, db)
              val searchedPostsFuture = PostModel.searchPostsByTitle(searchQuery.query, db)
              val searchedUsersFuture = UserModel.searchUsersByUsername(searchQuery.query, db)
              for {
                searchedBoards <- searchedBoardsFuture
                searchedPosts <- searchedPostsFuture
                searchedUsers <- searchedUsersFuture
              } yield {
                Ok(views.html.searchPage(emptySubs, searchedBoards, searchedPosts, searchedUsers, searchForm))
              }
          }
        }) 
      }.getOrElse {
        searchForm.bindFromRequest().fold(
        formWithErrors => {
          val boardsFuture = BoardModel.allBoards(db)
          boardsFuture.map(boards => Redirect(routes.UserController.homePage))
        },
        searchQuery => {
          val emptySubs: Seq[Subscription] = Seq()
          val searchedBoardsFuture = BoardModel.searchBoardsByTitle(searchQuery.query, db)
          val searchedPostsFuture = PostModel.searchPostsByTitle(searchQuery.query, db)
          val searchedUsersFuture = UserModel.searchUsersByUsername(searchQuery.query, db)
          for {
            searchedBoards <- searchedBoardsFuture
            searchedPosts <- searchedPostsFuture
            searchedUsers <- searchedUsersFuture
          } yield {
            Ok(views.html.searchPage(emptySubs, searchedBoards, searchedPosts, searchedUsers, searchForm))
          }
      })
    }
  }

}