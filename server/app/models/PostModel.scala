package models

import javax.inject.Inject
import javax.inject.Singleton
import play.api.data._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.data.Forms.number
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents
import scala.collection.mutable.Map
import scala.collection.mutable.Buffer

import slick.jdbc.MySQLProfile.api._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import controllers.Login
import controllers.NewPost

object PostModel {
  import Tables._
  
  def getPostsFromBoard(boardID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Post]] = {
    db.run {
      posts.filter(_.boardID === boardID).result
    }
  }

  def getPostFromID(postID: Int, db: Database)(implicit ec: ExecutionContext): Future[Option[Post]] = {
    db.run{
      posts.filter(_.id === postID).result.headOption
    }
  }
  
  def getPostFromTitle(title: String, db: Database)(implicit ec: ExecutionContext): Future[Option[Post]] = {
    db.run {
      posts.filter(_.title === title).result.headOption
    }
  }

  def addPost(boardID: Int, posterID: Int, np: NewPost, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      //first number is an id which the database will ignore and last two are upvotes and downvotes which database should ignore
      posts += Post(0, boardID, posterID, np.title, np.body, np.link, 0)
    }
  }
  def searchPostsByTitle(title: String, db: Database)(implicit ec: ExecutionContext): Future[Seq[Post]] = {
    db.run {
      posts.filter(_.title like title+"%").result
    }
  }
  
  def checkIfVoteExists(userID: Int, postID: Int, db: Database)(implicit ec: ExecutionContext): Future[Boolean] = {
    db.run {
      votePosts.filter(_.userID === userID).filter(_.postID === postID).exists.result
    }
  }
  
  def upvotePostDB(userID: Int, postID: Int, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      votePosts += VotePost(0, postID, userID, true)
    }
    calculateKarma(postID, db)
  }
  
  def downvotePostDB(userID: Int, postID: Int, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      votePosts += VotePost(0, postID, userID, false)
    }
    calculateKarma(postID, db)
  }
  
  def calculateKarma(postID: Int, db: Database)(implicit ec: ExecutionContext) = {
    val positiveKarma = 0
    val negativeKarma = 0
    val upvotes = for {
      p <- votePosts if p.postID === postID && p.upvote === true 
    } yield {
      positiveKarma + 1
    }
    val downvotes = for {
      p <- votePosts if p.postID === postID && p.upvote === false
    } yield {
      negativeKarma + 1
    }
    val totalKarma = positiveKarma - negativeKarma
    val updateQuery = posts.filter(_.id === postID).map(_.upvotes)
    db.run {
      updateQuery.update(totalKarma)
    }
  }
}
