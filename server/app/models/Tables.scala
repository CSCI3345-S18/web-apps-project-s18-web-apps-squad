package models

case class Post(id: Int, boardName: String, title: String, body: String, poster: String)
case class User(username: String, password: String, email: String)
case class Board(title: String, description: String)
case class Comment(id: Int, flag: Char, postParentID: Int, commentParentID: Int)
case class Messages(id: Int, senderID: Int, receiverID: Int, messages: String)
case class Subscription(id: Int, userID: Int, boardID: Int)

object Tables extends {
  val profile = slick.jdbc.MySQLProfile
  import profile.api._

  class Users(tag: Tag) extends Table[User](tag, "Users") {
    def username = column[String]("username")
    def password = column[String]("password")
    def email = column[String]("email")
    def * = (username, password, email) <> (User.tupled, User.unapply)
  }
  val users = TableQuery[Users]

  class Boards(tag: Tag) extends Table[Board](tag, "Boards") {
    def title = column[String]("title")
    def description = column[String]("description")
    def * = (title, description) <> (Board.tupled, Board.unapply)
  }
  val boards = TableQuery[Boards]
}
