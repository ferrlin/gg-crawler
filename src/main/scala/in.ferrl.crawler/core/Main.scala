package in.ferrl.crawler.core

import akka.actor.{ Props, ActorSystem }
import akka.pattern.ask
import in.ferrl.crawler.pattern.WorkPulling._

object Main extends App {

  start()

  def start() {

    import NaiveCrawler._
    import akka.util.Timeout
    import scala.concurrent.duration._

    implicit val timeout = Timeout(4 seconds)

    val ggSystem = ActorSystem("ggSystem")

    // val naive = ggSystem.actorOf(Props[NaiveCrawler], "naive")

    def newEpic[T](work: T) = new Epic[T] { override def iterator = Seq(work).iterator }

    val naive = ggSystem.actorOf(Props[NaiveCrawler], "master")
    val getter = ggSystem.actorOf(Props(new GetContent(naive)), "worker-1")

    Thread.sleep(1000)
    naive ! newEpic(GET("http://ferrl.in"))

    val f = naive ? newEpic(GET("http://ferrl.in"))

    // statement to expect a response after sending a crawl message
    // naive ? GET("http://ferrl.in")
    Thread.sleep(3000)
    ggSystem.shutdown()
  }
}