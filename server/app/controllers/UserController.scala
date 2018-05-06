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

@Singleton
class UserController @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    mcc: MessagesControllerComponents) (implicit ec: ExecutionContext)
    extends MessagesAbstractController(mcc) with HasDatabaseConfigProvider[JdbcProfile] {

  val newUserForm = Form(mapping(
      "email" -> nonEmptyText,
      "username" -> nonEmptyText,
      "password" -> nonEmptyText)(NewUser.apply)(NewUser.unapply))

  val loginForm = Form(mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText)(Login.apply)(Login.unapply))

  val searchForm = Form(mapping(
      "query" -> nonEmptyText)(SearchQuery.apply)(SearchQuery.unapply))

  def homePage() = Action.async { implicit request =>
    request.session.get("connected").map { user =>
      val loggedinUser = UserModel.getUserFromUsername(user, db)
      loggedinUser.flatMap {
        case Some(actualUser) =>
//          val subscribedBoards = UserModel.getSubscriptionsOfUser(actualUser.id, db)
//          val postsFromSubbedBoards = PostModel.getPostsFromSubscriptions(actualUser.id, db)
          val subscribedBoards = UserModel.getSubscriptionsOfUser(actualUser.id, db)
          val postsFromSubbedBoards = PostModel.allPosts(db)
          for {
            subs <- subscribedBoards
            posts <- postsFromSubbedBoards
          } yield {
            Ok(views.html.homePage(subs, posts, searchForm))
          }
        case None =>
          val emptySubs: Seq[Subscription] = Seq()
          val postsFromSubbedBoards = PostModel.allPosts(db)
          for {
            posts <- postsFromSubbedBoards
          } yield {
            Ok(views.html.homePage(emptySubs, posts, searchForm))
          }
      }
    }.getOrElse {
      val emptySubs: Seq[Subscription] = Seq()
      val postsFromSubbedBoards = PostModel.allPosts(db)
      for {
        posts <- postsFromSubbedBoards
      } yield {
        Ok(views.html.homePage(emptySubs, posts, searchForm))
      }
    }
  }

  def allUsers = Action.async { implicit request =>
    request.session.get("connected").map { user =>
      val loggedinUser = UserModel.getUserFromUsername(user, db)
      loggedinUser.flatMap {
        case Some(actualUser) =>
          val subscribedBoards = UserModel.getSubscriptionsOfUser(actualUser.id, db)
          val usersFuture = UserModel.allUsers(db)
          for {
            boards <- subscribedBoards
            users <- usersFuture
          } yield {
            Ok(views.html.loginPage(boards, users, loginForm, newUserForm, searchForm))
          }
        case None =>
          val emptySubs: Seq[Subscription] = Seq()
          val usersFuture = UserModel.allUsers(db)
          usersFuture.map(users => Ok(views.html.loginPage(emptySubs, users, loginForm, newUserForm, searchForm)))
      }
    }.getOrElse {
      val emptySubs: Seq[Subscription] = Seq()
      val usersFuture = UserModel.allUsers(db)
      usersFuture.map(users => Ok(views.html.loginPage(emptySubs, users, loginForm, newUserForm, searchForm)))
    }
  }

  def addUser = Action.async { implicit request =>
    newUserForm.bindFromRequest().fold(
        formWithErrors => {
          val usersFuture = UserModel.allUsers(db)
          val subscribedBoards: Seq[Subscription] = Seq()
          for {
            users <- usersFuture
          } yield (BadRequest(views.html.loginPage(subscribedBoards, users, loginForm, formWithErrors, searchForm)))
        },
        newUser => {
          val checkUsername = UserModel.checkIfUsernameExists(newUser.username, db)
          checkUsername.map(success =>
            if(!success) {
              UserModel.addUser(newUser, db)
              Redirect(routes.UserController.profilePage).withSession("connected" -> newUser.username)
            } else {
              Redirect(routes.UserController.loginPage)
            })
        })
  }

  def login = Action.async { implicit request =>
    loginForm.bindFromRequest().fold(
        formWithErrors => {
          val usersFuture = UserModel.allUsers(db)
          val subscribedBoards: Seq[Subscription] = Seq()
          for {
            users <- usersFuture
          } yield (BadRequest(views.html.loginPage(subscribedBoards, users, formWithErrors, newUserForm, searchForm)))
        },
        loginUser => {
          val loginFuture = UserModel.verifyUser(loginUser, db)
          loginFuture.map { success =>
            if(success == true) Redirect(routes.UserController.profilePage).withSession("connected" -> loginUser.username)
            else Redirect(routes.UserController.allUsers).flashing("error" -> "Failed to login.")
          }
        })
  }

  def logout = Action { implicit request =>
    Redirect(routes.UserController.allUsers).withNewSession
  }

  def userPage(username: String) = Action.async { implicit request =>
    request.session.get("connected"). map { user =>
      val loggedinUser = UserModel.getUserFromUsername(user, db)
      loggedinUser.flatMap {
        case Some(actualUser) =>
          val loggedSubs = UserModel.getSubscriptionsOfUser(actualUser.id, db)
          val userContents = UserModel.getUserFromUsername(username, db)
          userContents.flatMap {
            case Some(viewedUser) =>
              val postsSeqOpt = UserModel.getPostsOfUser(viewedUser.id, db)
              val commentsSeqOpt = UserModel.getCommentsOfUser(viewedUser.id, db)
              val karmaOut = UserModel.getKarma(viewedUser.id, db)
              for {
                subs <- loggedSubs
                posts <- postsSeqOpt
                comments <- commentsSeqOpt
                karma <- karmaOut
              } yield {
                Ok(views.html.userPage(viewedUser.username, subs, posts, comments, karma, searchForm))
              }
            case None =>
              Future.successful(Redirect(routes.UserController.homePage))
          }
        case None =>
          val userContents = UserModel.getUserFromUsername(username, db)
          userContents.flatMap {
            case Some(viewedUser) =>
              val postsSeqOpt = UserModel.getPostsOfUser(viewedUser.id, db)
              val commentsSeqOpt = UserModel.getCommentsOfUser(viewedUser.id, db)
              val emptySubs: Seq[Subscription] = Seq()
              val karmaOut = UserModel.getKarma(viewedUser.id, db)
              for {
                posts <- postsSeqOpt
                comments <- commentsSeqOpt
                karma <- karmaOut
              } yield {
                Ok(views.html.userPage(viewedUser.username, emptySubs, posts, comments, karma, searchForm))
              }
            case None =>
              Future.successful(Redirect(routes.UserController.homePage))
          }
      }
      }.getOrElse {
          val userContents = UserModel.getUserFromUsername(username, db)
          userContents.flatMap {
            case Some(viewedUser) =>
              val postsSeqOpt = UserModel.getPostsOfUser(viewedUser.id, db)
              val commentsSeqOpt = UserModel.getCommentsOfUser(viewedUser.id, db)
              val karmaOut = UserModel.getKarma(viewedUser.id, db)
              val emptySubs: Seq[Subscription] = Seq()
              for {
                posts <- postsSeqOpt
                comments <- commentsSeqOpt
                karma <- karmaOut
              } yield {
                Ok(views.html.userPage(viewedUser.username, emptySubs, posts, comments, karma, searchForm))
              }
            case None =>
              Future.successful(Redirect(routes.UserController.homePage))
          }
    }
  }

  def profilePage = Action.async { implicit request =>
    request.session.get("connected").map { user =>
      val loggedinUser = UserModel.getUserFromUsername(user, db)
      loggedinUser.flatMap {
        case Some(actualUser) =>
          val postsSeqOpt = UserModel.getPostsOfUser(actualUser.id, db)
          val commentsSeqOpt = UserModel.getCommentsOfUser(actualUser.id, db)
          val subBoardsSeqOpt = UserModel.getSubscriptionsOfUser(actualUser.id, db)
          val karmaOut = UserModel.getKarma(actualUser.id, db)
          for {
            posts <- postsSeqOpt
            comments <- commentsSeqOpt
            subBoards <- subBoardsSeqOpt
            karma <- karmaOut
          } yield {
            Ok(views.html.profilePage(actualUser.username, posts, comments, subBoards, karma, searchForm))
          }
        case None =>
          Future.successful(Redirect(routes.UserController.loginPage))
      }
    }.getOrElse {
      val boardsFuture = BoardModel.allBoards(db)
      boardsFuture.map(boards => Redirect(routes.UserController.loginPage))
    }
  }

  def loginPage() = Action.async { implicit request =>
    request.session.get("connected").map { user =>
      val loggedinUser = UserModel.getUserFromUsername(user, db)
      loggedinUser.flatMap {
        case Some(actualUser) =>
          Future.successful(Redirect(routes.UserController.profilePage))
        case None =>
          val usersFuture = UserModel.allUsers(db)
          val boards: Seq[Subscription] = Seq()
          usersFuture.map(users => Ok(views.html.loginPage(boards, users, loginForm, newUserForm, searchForm)))
      }
    }.getOrElse {
      val usersFuture = UserModel.allUsers(db)
      val boards: Seq[Subscription] = Seq()
      for {
        users <- usersFuture
      } yield(Ok(views.html.loginPage(boards, users, loginForm, newUserForm, searchForm)))
    }
  }
}
