package in.ferrl.crawler.core

import akka.actor.{ Props, ActorSystem }
import in.ferrl.crawler.pattern.WorkPulling._

object Main extends App {

  start()

  def start() {

    import NaiveCrawler._

    val ggSystem = ActorSystem("gg-crawler-system")

    // val naive = ggSystem.actorOf(Props[NaiveCrawler], "naive")

    def newEpic[T](work: T) = new Epic[T] { override def iterator = Seq(work).iterator }

    val naive = ggSystem.actorOf(Props[NaiveCrawler])
    val getter = ggSystem.actorOf(Props(new GetContent(naive)))

    Thread.sleep(5000)
    naive ! newEpic(GET("http://ferrl.in/"))

    // statement to expect a response after sending a crawl message
    // naive ? GET("http://ferrl.in")
  }
}