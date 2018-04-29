package models

object Tables extends {
  val profile = slick.jdbc.MySQLProfile
  import profile.api._
  
  class Users(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Int]("id")
    def username = column[String]("username")
    def password = column[String]("password")
    def email = column[String]("email")
    def * = (id, username, password, email) <> (User.tupled, User.unapply)
  }
  val users = TableQuery[Users]
}