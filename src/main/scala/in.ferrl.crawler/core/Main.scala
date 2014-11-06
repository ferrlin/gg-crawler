package in.ferrl.crawler.core

import akka.actor.{ Props, ActorSystem }

object Main extends App {

  start()

  def start() {

    import NaiveCrawler._

    val ggSystem = ActorSystem("gg-crawler-system")
    val naive = ggSystem.actorOf(Props[NaiveCrawler], "naive")

    naive ! GET("http://ferrl.in/", 3)

    // statement to expect a response after sending a crawl message
    // naive ? GET("http://ferrl.in")
  }
}