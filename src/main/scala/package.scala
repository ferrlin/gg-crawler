package object gg {
  val USER_AGENT = "User-agent:"
  val DISALLOW = "Disallow:"
  val REGEXP_HTTP = "<a href=\"http://(.)*\">"
  val REGEXT_RELATIVE = "<a href=\"(.)*\">"

  object crawler {
    sealed trait Task
    /**
     * The `proceed parameter will flag if
     * after completing one task it should
     * automatically moves to the next.
     */
    case class Fetch(url: String, depth: Int, proceed: Boolean) extends Task
    case class Parse(id: String, proceed: Boolean) extends Task
    case class Index(id: String) extends Task
    case class Completed[T <: Task](task: T, id: String, result: Any) extends Task
    case class Failed(message: String) extends Task
  }
}
