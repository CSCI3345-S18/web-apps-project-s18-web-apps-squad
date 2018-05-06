package spa

import org.scalajs.dom
import dom.document
import scala.scalajs.js.annotation.JSExportTopLevel
import org.querki.jquery._
import scala.scalajs.js.JSON
import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal
import org.scalajs.dom.raw.WebSocket

object SPAMain {

  def main(args: Array[String]): Unit = {
    val window = document.defaultView
    val socket = new WebSocket("ws://" + window.location.hostname + ":" + window.location.port + "/messagingsocket")
    socket.addEventListener("message", {(event: dom.MessageEvent) =>

    })
  }
}
