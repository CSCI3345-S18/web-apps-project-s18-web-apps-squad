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
import controllers.NewComment

object CommentModel {
  import Tables._

  def getCommentsFromPost(postParentID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Comment]] = {
    db.run {
      comments.filter(_.postParentID === postParentID).result
    }
  }
  def getCommentsFromUser(userID: Int, db: Database)(implicit ec: ExecutionContext): Future[Seq[Comment]] = {
    db.run {
      comments.filter(_.userID === userID).result
    }
  }
  def addComment(body: String, userID: Int, username: String, postParentID: Int, flag: Char, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      //0 for id upvotes and downvotes
      comments += Comment(0, body, userID, username, postParentID, flag, 0)
    }
  }

  def getCommentFromID(id: Int, db: Database)(implicit ec: ExecutionContext): Future[Option[Comment]] = {
    db.run {
      comments.filter(_.id === id).result.headOption
    }
  }

  def checkIfVoteExists(userID: Int, commentID: Int, db: Database)(implicit ec: ExecutionContext): Future[Boolean] = {
    db.run {
      voteComments.filter(_.userID === userID).filter(_.commentID === commentID).exists.result
    }
  }

  def upvoteCommentDB(userID: Int, commentID: Int, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      voteComments += VoteComment(0, commentID, userID, true)
    }
    calculateKarma(commentID, db)
  }
  def updateVote(userID: Int, commentID: Int, upvote: Boolean, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run{
      voteComments.filter(_.userID === userID).filter(_.commentID === commentID).map(_.upvote).update(upvote)
    }
    calculateKarma(commentID, db)
  }
  def deleteComment(commentID: Int, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run{
      comments.filter(_.id === commentID).delete
    }
  }
  def downvoteCommentDB(userID: Int, commentID: Int, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    db.run {
      voteComments += VoteComment(0, commentID, userID, false)
    }
    calculateKarma(commentID, db)
  }

  def calculateKarma(commentID: Int, db: Database)(implicit ec: ExecutionContext): Future[Int] = {
    val q1 = voteComments.filter(_.commentID === commentID)
    val positiveKarmaData:Future[Int] = db.run(q1.filter(_.upvote === true).length.result)
    val negativeKarmaData:Future[Int] = db.run(q1.filter(_.upvote === false).length.result)

    for{
      positiveKarma <- positiveKarmaData
      negativeKarma <- negativeKarmaData
    } yield{
      val totalKarma = positiveKarma - negativeKarma
      val updateQuery = comments.filter(_.id === commentID).map(_.upvotes)
      db.run {
        updateQuery.update(totalKarma)
      }
      totalKarma
    }
  }
}
