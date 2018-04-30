package models

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
