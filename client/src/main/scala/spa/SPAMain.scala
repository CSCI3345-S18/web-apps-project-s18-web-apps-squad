package spa

import org.scalajs.dom
import dom.document
import scala.scalajs.js.annotation.JSExportTopLevel
import org.querki.jquery._
import scala.scalajs.js.JSON
import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal
import org.scalajs.dom.raw.WebSocket
import org.scalajs.dom.raw.Event

object SPAMain {

  def main(args: Array[String]): Unit = {
    var msg = $("#msg").value().asInstanceOf[String]
    val window = document.defaultView
    val socket = new WebSocket("ws://" + window.location.hostname + ":" + window.location.port + "/messagingsocket")
    
    val myUID = document.getElementById("myUID").getAttribute("value").toInt
    val friendsUID = document.getElementById("friendsUID").getAttribute("value").toInt

    socket.addEventListener("open", {(event: dom.MessageEvent) => socket.send(myUID + ", " + friendsUID)})

    socket.addEventListener("message", {(event: dom.MessageEvent) =>
      val data = JSON.parse(event.data.toString())
      val sender = data.userOneID.asInstanceOf[Int]
      val receiver = data.userTwoID.asInstanceOf[Int]
      val message = data.body.asInstanceOf[String]
        
      $("#profile-messages").append("<p align=\"left\">" + message + "</p>")
    })
    
    $("#msg").change(() => msg = $("#msg").value().asInstanceOf[String])
    
    $("#sendButton").click{() =>
      val jsonLit = js.Dynamic.literal(userOneID = myUID, userTwoID = friendsUID, body = msg)
      socket.send(JSON.stringify(jsonLit))
      $("#profile-messages").append("<p align=\"right\">" + msg + "</p>")
    }   
    
    $(".messages").each{ elem =>
      //println(elem.getAttribute("value").toString())
      val data = JSON.parse(elem.getAttribute("value").toString())
      val message = data.message.asInstanceOf[String]
      println(message)
      val sender = data.userOneID.asInstanceOf[Int]
      println(sender)
      val htmlElem = {
          if(sender == myUID){
						"<p class=\"messages\" align=\"right\">" + message + "</p>"
					} else{
						"<p class=\"messages\" align=\"left\">" + message + "</p>"
					}
      }
      $("#profile-messages").append(htmlElem)
    }
  }
}