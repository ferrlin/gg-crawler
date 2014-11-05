package in.ferrl.crawler.core

import akka.actor.{ Props, ActorSystem }

object Main extends App {

  startProcess()

  def startProcess() {

  	import NaiveCrawler._

    val ggSystem = ActorSystem("gg-crawler-system")

    val naive = ggSystem.actorOf(Props[NaiveCrawler], "naive")

    naive ! Crawl(CrawlerUrl("http://ferrl.in", 5, false))

    // statement to expect a response after sending a crawl message
    // naive ? Crawl(CrawlerUrl("http://ferrl.in", 5, false))
  }
}