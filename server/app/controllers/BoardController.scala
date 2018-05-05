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
//import controllers.NewBoard
import models.Post
import models.Subscription
import models.SubscriptionModel

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
      
  val searchForm = Form(mapping(
      "query" -> nonEmptyText)(SearchQuery.apply)(SearchQuery.unapply))
      
  def allBoards = Action.async { implicit request =>
    request.session.get("connected").map { user =>
      val loggedinUser = UserModel.getUserFromUsername(user, db)
      loggedinUser.flatMap {
        case Some(actualUser) =>
          val subbedBoardsFut = UserModel.getSubscriptionsOfUser(actualUser.id, db)
          val popularBoardsFut = BoardModel.allBoards(db)
          for {
            subs <- subbedBoardsFut
            boards <- popularBoardsFut
          } yield {
            Ok(views.html.popularBoardsPage(subs, boards, searchForm))
          }
        case None =>
          val popularBoardsFut = BoardModel.allBoards(db)
          val emptySubs: Seq[Subscription] = Seq()
          for {
            boards <- popularBoardsFut
          } yield {
            Ok(views.html.popularBoardsPage(emptySubs, boards, searchForm))
          }
      }
    }.getOrElse {
      val boardsFuture = BoardModel.allBoards(db)
      val emptySubs: Seq[Subscription] = Seq()
      boardsFuture.map(boards => Ok(views.html.popularBoardsPage(emptySubs, boards, searchForm)))
    }
  }

  def addBoard = Action.async { implicit request =>
    request.session.get("connected").map { user =>
      val loggedinUser = UserModel.getUserFromUsername(user, db)
      newBoardForm.bindFromRequest().fold(
        formWithErrors => {
          loggedinUser.flatMap {
            case Some(actualUser) =>
              val subscribedBoardsFut = UserModel.getSubscriptionsOfUser(actualUser.id, db)
              subscribedBoardsFut.map(boards => BadRequest(views.html.addBoardPage(boards, newBoardForm, searchForm)))
            case None =>
              val emptySubs: Seq[Subscription] = Seq()
              Future.successful(Ok(views.html.addBoardPage(emptySubs, newBoardForm, searchForm)))
          }
        },
        newBoard => {
          val addFuture = BoardModel.addBoard(newBoard, db)
          val addedBoard = BoardModel.getBoardByTitle(newBoard.title, db)
          loggedinUser.flatMap {
            case Some(user) =>
              addedBoard.flatMap {
                case Some(board) =>
                  addFuture.map { cnt =>
                    SubscriptionModel.addSubscription(user.id, board.id, board.title, db)
                    if(cnt == 1) Redirect(routes.BoardController.boardPage(board.title))
                    else Redirect(routes.BoardController.allBoards).flashing("error" -> "Failed to add board.")
                  }
                case None =>
                  Future.successful(Redirect(routes.BoardController.allBoards))
              }
            case None =>
              Future.successful(Redirect(routes.BoardController.allBoards))
          }
          addFuture.map { cnt =>
              if(cnt == 1) Redirect(routes.BoardController.boardPage(newBoard.title))
              else Redirect(routes.BoardController.allBoards).flashing("error" -> "Failed to add board.")
          }
        })
    }.getOrElse {
      val boardsFuture = BoardModel.allBoards(db)
      boardsFuture.map(boards => Redirect(routes.UserController.loginPage))
    }
  }

  //Gets the board in question as well as loads the posts from the board
  def boardPage(title: String) = Action.async { implicit request =>
    request.session.get("connected").map { user =>
      val loggedinUser = UserModel.getUserFromUsername(user, db)
      loggedinUser.flatMap {
        case Some(actualUser) =>
          val boardsFutOpt = BoardModel.getBoardByTitle(title, db)
          boardsFutOpt.flatMap {
            case Some(board) =>
              val postsSeqOpt = PostModel.getPostsFromBoard(board.id, db)
              val userSubs = UserModel.getSubscriptionsOfUser(actualUser.id, db)
              for {
                posts <- postsSeqOpt
                subs <- userSubs
              } yield {
                Ok(views.html.boardPage(posts, subs, board.title, board.description, searchForm))
              }
            case None =>
              Future.successful(Redirect(routes.UserController.homePage))
          }
        case None =>
          val boardsFutOpt = BoardModel.getBoardByTitle(title, db)
          boardsFutOpt.flatMap {
            case Some(board) =>
              val postsSeqOpt = PostModel.getPostsFromBoard(board.id, db)
              val emptySubs: Seq[Subscription] = Seq()
              postsSeqOpt.map(posts => Ok(views.html.boardPage(posts, emptySubs, board.title, board.description, searchForm)))
            case None =>
              val boardsFutOpt = BoardModel.getBoardByTitle(title, db)
              boardsFutOpt.map(boards => Redirect(routes.UserController.homePage))
          }
      }
    }.getOrElse {
      val boardsFutOpt = BoardModel.getBoardByTitle(title, db)
      boardsFutOpt.flatMap {
        case Some(board) =>
          val postsSeqOpt = PostModel.getPostsFromBoard(board.id, db)
          val emptySubs: Seq[Subscription] = Seq()
          postsSeqOpt.map(posts => Ok(views.html.boardPage(posts, emptySubs, board.title, board.description, searchForm)))
        case None =>
          val boardsFutOpt = BoardModel.getBoardByTitle(title, db)
          boardsFutOpt.map(boards => Redirect(routes.UserController.homePage))
      }
    }
  }
  
  def addBoardPage() = Action.async { implicit request =>
    request.session.get("connected").map { user =>
      val loggedinUser = UserModel.getUserFromUsername(user, db)
      loggedinUser.flatMap {
        case Some(actualUser) =>
          val subscribedBoards = UserModel.getSubscriptionsOfUser(actualUser.id, db)
          subscribedBoards.map(boards => Ok(views.html.addBoardPage(boards, newBoardForm, searchForm)))
        case None =>
          val emptySubs: Seq[Subscription] = Seq()
          Future.successful(Ok(views.html.addBoardPage(emptySubs, newBoardForm, searchForm)))
      }
    }.getOrElse {
      val emptySubs: Seq[Subscription] = Seq()
      Future.successful(Ok(views.html.addBoardPage(emptySubs, newBoardForm, searchForm)))
    }
  }

}
