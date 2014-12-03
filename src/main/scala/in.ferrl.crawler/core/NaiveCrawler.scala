package in.ferrl.crawler.core

import akka.actor.{ Actor, Props }
import akka.util.Timeout
import akka.event.Logging
import in.ferrl.crawler.pattern.Master
import in.ferrl.crawler.pattern.WorkPulling._

object NaiveCrawler {

  val USER_AGENT = "User-agent:"
  val DISALLOW = "Disallow:"
  val REGEXP_HTTP = "<a href=\"http://(.)*\">"
  val REGEXT_RELATIVE = "<a href=\"(.)*\">"

  sealed trait ggTask

  case class Fetch(url: String, depth: Int, metadata: Map[String, Any]) extends ggTask
  case class FetchComplete(id: String) extends ggTask
  case class Parse(id: String) extends ggTask
  case class ParseComplete(id: String) extends ggTask
  case class Index(id: String) extends ggTask
  case class IndexComplete(id: String) extends ggTask
}

import NaiveCrawler._

class NaiveCrawler extends Master[ggTask] {}