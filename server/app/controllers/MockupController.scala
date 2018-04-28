package controllers

import javax.inject.Inject
import javax.inject.Singleton
import play.api.mvc.MessagesAbstractController
import play.api.mvc.MessagesControllerComponents

@Singleton
class MockupController @Inject() (cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  def homePage() = Action { implicit request =>
    Ok(views.html.homePage())
  }
  
  def userPage() = Action { implicit request =>
    Ok(views.html.userPage())
  }
  
  def postPage() = Action { implicit request =>
    Ok("")//Ok(views.html.postPage())
  }
  
}