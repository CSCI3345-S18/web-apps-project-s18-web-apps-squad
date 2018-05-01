package models

object PostModel {
  def getPostsFromBoard(boardID: Int) = {
    // Pull all posts from the given board
  }
  
  def getPostFromPostID(boardName: String, postID: Int): Option[Post] = {
    return Some(Post(postID, boardName, "TODO in DBData val postName", "TODO in DBData val postBody", "TODO in DBData val poster"))
  }
  
  def getCommentsFromPost(postID: Int): Seq[(String, String)] = {
    // Pull all comments from the particular post
    return Seq(("todo comment body", "todo commenter"))//feel free to make this a case class
  }  
  
  def addCommentToPost() = {
    // Store comment into a variable filled by user
    // Need a post to send comment to
  }
  
  def addPostToBoard() = {
    // Save post to something
    // Need a board to place post on
  }
}