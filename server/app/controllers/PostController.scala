package controllers

import javax.inject.Inject
import javax.inject.Singleton
import play.api.mvc.MessagesControllerComponents
import play.api.mvc.MessagesAbstractController
import models.DBData
import models._

//case class Post(id: Int, boardName: String, title: String, body: String, poster: String)

@Singleton
class PostController @Inject() (cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {
  val dataModel = new DBData()
  
  def viewPost(board: String, postIDStr: String) = Action { implicit request =>
    val boards: Seq[String] = request.session.get("username").map{username =>
					dataModel.getSubscriptionsFromUser(username)
				}.getOrElse{
					dataModel.getDefaultSubscription()
				}
    try{
      val postID = postIDStr.toInt
      val postOpt = dataModel.getPostFromPostID(board, postID)
      
      postOpt match {
        case None => throw new java.lang.NumberFormatException
        case Some(post) => {
          val comments = dataModel.getCommentsFromPost(postID)
          Ok(views.html.postPage(board, boards, post.title, post.body, post.poster, comments))
        }
        
      }
      
    } catch {
        case e: java.lang.NumberFormatException => Ok(views.html.errorPage("Post" + postIDStr + " not found", boards))
    } 
  }
  
}