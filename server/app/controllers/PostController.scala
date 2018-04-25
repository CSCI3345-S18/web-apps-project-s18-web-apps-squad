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
    var currentUser = "Login/Signup"
    request.session.get("username").map{ currentUserStr =>
      currentUser = currentUserStr
    }
    val currentUsersSubs = dataModel.getSubscriptionsFromUser(currentUser)
      try{
       val postID = postIDStr.toInt
       val postOpt = dataModel.getPostFromPostID(board, postID)
      
       postOpt match {
         case Some(post) => {
           val comments = Seq(("TODO comment", "TODO user"), ("TODO comment", "TODO user"))
           Ok(views.html.postTemplate(board, currentUser, currentUsersSubs, post.title, post.body, post.poster, comments))
         }
         case None => Ok(views.html.errorPage("Post" + postIDStr + " not found", currentUsersSubs, currentUser))
       }
      
     } catch {
       case e: java.lang.NumberFormatException => Ok(views.html.errorPage("Bad post name", currentUsersSubs, currentUser))
     } 
  }
}