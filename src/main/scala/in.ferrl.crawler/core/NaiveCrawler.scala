package in.ferrl.crawler.core

import akka.actor.{ Actor, Props }
import akka.util.Timeout
import akka.event.Logging
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

class NaiveCrawler extends Actor {

  import NaiveCrawler._
  val contentActor = context.actorOf(Props[GetContent], "GetContent")
  val saveActor = context.actorOf(Props[SaveContent], "SaveContent")
  val log = Logging(context.system, this)

  def receive: Receive = {
    case GET(url) ⇒ {
      log.info(s"Messaged received.. Initiating crawl to $url")
      contentActor ! Request(CrawlerUrl(url)) // Call GetContent actor
    }
    case Save ⇒ // Do nothing for now
    case _ ⇒ log.info("Do nothing for now")
  }
}