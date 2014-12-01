package in.ferrl.crawler.core

import akka.actor.{ Actor, Props }
import akka.util.Timeout
import akka.event.Logging
import in.ferrl.crawler.pattern.Master
import in.ferrl.crawler.pattern.WorkPulling._
/**
 * Represents the instance of the URL that is
 * visited by the crawler
 */
// case class CrawlerUrl(url: String, depth: Int = 1,
// allowedToVisit: Boolean = true, checkedForPermission: Boolean = false, visited: Boolean = false)

object NaiveCrawler {
  /**
   * Constants
   */
  val USER_AGENT = "User-agent:"
  val DISALLOW = "Disallow:"
  val REGEXP_HTTP = "<a href=\"http://(.)*\">"
  val REGEXT_RELATIVE = "<a href=\"(.)*\">"

  sealed trait ggMessages

  case class Fetch(url: String, depth: Int, metadata: Map[String, Any]) extends ggMessages
  case class FetchComplete(id: String) extends ggMessages
  case class Parse(id: String) extends ggMessages
  case class ParseComplete(id: String) extends ggMessages
  case class Index(id: String) extends ggMessages
  case class IndexComplete(id: String) extends ggMessages
}

import NaiveCrawler._

class NaiveCrawler extends Master[ggMessages] {
  /*
  def receive: Receive = {
    case GET(url) ⇒ {
      log.info(s"Messaged received.. Initiating crawl to $url")
      contentActor ! Request(CrawlerUrl(url)) // Call GetContent actor
    }
    case Save ⇒ // Do nothing for now
    case _ ⇒ log.info("Do nothing for now")
  }
  */
}