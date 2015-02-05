package object gg {
  val USER_AGENT = "User-agent:"
  val DISALLOW = "Disallow:"
  val REGEXP_HTTP = "<a href=\"http://(.)*\">"
  val REGEXT_RELATIVE = "<a href=\"(.)*\">"
  object crawler {
    sealed trait ggTask
    case class Fetch(url: String, depth: Int, metadata: List[(String, String)]) extends ggTask
    case class FetchComplete(id: String) extends ggTask
    case class Parse(id: String) extends ggTask
    case class ParseComplete(id: String) extends ggTask
    case class Index(id: String) extends ggTask
    case class IndexComplete(id: String) extends ggTask
  }
}
