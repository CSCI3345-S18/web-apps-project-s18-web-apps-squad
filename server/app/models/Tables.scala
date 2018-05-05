package models

case class Post(
  id: Int,
  boardID: Int,
  posterID: Int,
  title: String,
  body: String,
  link: String,
  upvotes: Int,
  downvotes: Int
)
case class User(
  id: Int,
  username: String,
  password: String,
  email: String
)
case class Board(
  id: Int,
  title: String,
  description: String
)
case class Comment(
  id: Int,
  body: String,
  userID: Int,
  postParentID: Int,
  flag: Char,
  upvotes: Int,
  downvotes: Int
)
case class Message(
  id: Int,
  body: String,
  senderID: Int,
  receiverID: Int
)
case class VotePost(
  id: Int,
  postID: Int,
  userID: Int,
  upvote: Boolean
)
case class VoteComment(
  id: Int,
  commentID: Int,
  userID: Int,
  upvote: Boolean
)
case class Subscription(
  id: Int,
  userID: Int,
  boardID: Int,
  title: String
)
case class Friendship(
  id: Int,
  userOneID: Int,
  userTwoID: Int
)

object Tables extends {
  val profile = slick.jdbc.MySQLProfile
  import profile.api._

  class Comments(tag: Tag) extends Table[Comment](tag, "Comments") {
    def id = column[Int]("id")
    def body = column[String]("body")
    def userID = column[Int]("user_id")
    def postParentID = column[Int]("post_parent_id")
    def flag = column[Char]("flag")
    def upvotes = column[Int]("total_upvotes")
    def downvotes = column[Int]("total_downvotes")
    def * = (id, body, userID, postParentID, flag, upvotes, downvotes) <> (Comment.tupled, Comment.unapply)
  }
  val comments = TableQuery[Comments]
  class Messages(tag: Tag) extends Table[Message](tag, "Messages") {
    def id = column[Int]("id")
    def body = column[String]("body")
    def senderID = column[Int]("sender_id")
    def receiverID = column[Int]("receiver_id")
    def * = (id, body, senderID, receiverID) <> (Message.tupled, Message.unapply)
  }
  val messages = TableQuery[Messages]
  class Subscriptions(tag: Tag) extends Table[Subscription](tag, "Subscriptions") {
    def id = column[Int]("id")
    def userID = column[Int]("user_id")
    def boardID = column[Int]("board_id")
    def title = column[String]("title")
    def * = (id, userID, boardID, title) <> (Subscription.tupled, Subscription.unapply)
  }
  val subscriptions = TableQuery[Subscriptions]
  class VotePosts(tag: Tag) extends Table[VotePost](tag, "Vote_Posts") {
    def id = column[Int]("id")
    def userID = column[Int]("post_id")
    def boardID = column[Int]("board_id")
    def upvote = column[Boolean]("upvote")
    def * = (id, userID, boardID, upvote) <> (VotePost.tupled, VotePost.unapply)
  }
  val votePosts = TableQuery[VotePosts]
  class VoteComments(tag: Tag) extends Table[VoteComment](tag, "Vote_Comment") {
    def id = column[Int]("id")
    def userID = column[Int]("post_id")
    def boardID = column[Int]("board_id")
    def upvote = column[Boolean]("upvote")
    def * = (id, userID, boardID, upvote) <> (VoteComment.tupled, VoteComment.unapply)
  }
  val voteComments = TableQuery[VoteComments]
  class Users(tag: Tag) extends Table[User](tag, "Users") {
    def id = column[Int]("id")
    def username = column[String]("username")
    def password = column[String]("password")
    def email = column[String]("email")
    def * = (id, username, password, email) <> (User.tupled, User.unapply)
  }
  val users = TableQuery[Users]

  class Boards(tag: Tag) extends Table[Board](tag, "Boards") {
    def id = column[Int]("id")
    def title = column[String]("title")
    def description = column[String]("description")
    def * = (id, title, description) <> (Board.tupled, Board.unapply)
  }
  val boards = TableQuery[Boards]

  class Posts(tag: Tag) extends Table[Post](tag, "Posts") {
    def id = column[Int]("id")
    def boardID = column[Int]("board_id")
    def posterID = column[Int]("poster_id")
    def title = column[String]("title")
    def body = column[String]("body")
    def link = column[String]("link")
    def upvotes = column[Int]("total_upvotes")
    def downvotes = column[Int]("total_downvotes")
    def * = (id, boardID, posterID, title, body, link, upvotes, downvotes) <> (Post.tupled, Post.unapply)
  }
  val posts = TableQuery[Posts]
  class Friends(tag: Tag) extends Table[Friendship](tag, "Friends"){
    def id = column[Int]("id")
    def userOneID = column[Int]("user_one_id")
    def userTwoID = column[Int]("user_two_id")
    def * = (id, userOneID, userTwoID) <> (Friendship.tupled, Friendship.unapply)
  }
  val friends = TableQuery[Friends]
}
