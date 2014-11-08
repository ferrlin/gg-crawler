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
case class CrawlerUrl(url: String, depth: Int = 1,
  allowedToVisit: Boolean = true, checkedForPermission: Boolean = false, visited: Boolean = false)

object NaiveCrawler {
  /**
   * Constants
   */
  val USER_AGENT = "User-agent:"
  val DISALLOW = "Disallow:"
  val REGEXP_HTTP = "<a href=\"http://(.)*\">"
  val REGEXT_RELATIVE = "<a href=\"(.)*\">"

  /**
   * Messages for our crawler
   */
  case class GET(url: String)
  case class Request(url: CrawlerUrl)
  case object Save
}

class NaiveCrawler extends Master[CrawlerUrl] {

  import NaiveCrawler._

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