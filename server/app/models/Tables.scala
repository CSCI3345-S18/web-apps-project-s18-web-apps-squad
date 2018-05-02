package models

case class Post(id: Int, boardID: Int, posterID: Int,
   title: String, body: String, link: String, upvotes: Int, downvotes: Int)
case class User(id: Int, username: String, password: String, email: String)
case class Board(title: String, description: String)
case class Comment(id: Int, flag: Char, postParentID: Int, commentParentID: Int)
case class Messages(id: Int, senderID: Int, receiverID: Int, messages: String)
case class Subscription(id: Int, userID: Int, boardID: Int)

object Tables extends {
  val profile = slick.jdbc.MySQLProfile
  import profile.api._

  class Users(tag: Tag) extends Table[User](tag, "Users") {
    def id = column[Int]("id")
    def username = column[String]("username")
    def password = column[String]("password")
    def email = column[String]("email")
    def * = (id, username, password, email) <> (User.tupled, User.unapply)
  }
  val users = TableQuery[Users]

  class Boards(tag: Tag) extends Table[Board](tag, "Boards") {
    def title = column[String]("title")
    def description = column[String]("description")
    def * = (title, description) <> (Board.tupled, Board.unapply)
  }
  val boards = TableQuery[Boards]

  class Posts(tag: Tag) extends Table[Post](tag, "Posts") {
    def id = column[Int]("id")
    def boardID = column[Int]("boardID")
    def posterID = column[Int]("posterID")
    def title = column[String]("title")
    def body = column[String]("body")
    def link = column[String]("link")
    def upvotes = column[Int]("upvotes")
    def downvotes = column[Int]("downvotes")
    def * = (id, boardID, posterID, title, body, link, upvotes, downvotes) <> (Post.tupled, Post.unapply)
  }
  val posts = TableQuery[Posts]
}
