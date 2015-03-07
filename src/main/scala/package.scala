package object gg {
  val USER_AGENT = "User-agent:"
  val DISALLOW = "Disallow:"
  val REGEXP_HTTP = "<a href=\"http://(.)*\">"
  val REGEXT_RELATIVE = "<a href=\"(.)*\">"

  object crawler {
    sealed trait Task
    case class Fetch(url: String, depth: Int, metadata: List[(String, String)]) extends Task
    case class Parse(id: String) extends Task
    case class Index(id: String) extends Task
    case class Completed[T <: Task](task: T, id: String, result: Any) extends Task
    case class Failed[T <: Task](task: T, id: String) extends Task
  }
}
