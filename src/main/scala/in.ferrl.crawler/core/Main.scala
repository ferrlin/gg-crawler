package in.ferrl.crawler.core

import akka.actor.{ Props, ActorSystem }
import scala.util.{ Success, Failure }
import akka.pattern.ask
import in.ferrl.crawler.pattern.WorkPulling._

object Main extends App {

  start()

  def start() {

    import akka.util.Timeout
    import scala.concurrent.duration._

    implicit val timeout = Timeout(4 seconds)

    val ggSystem = ActorSystem("ggSystem")

    // val naive = ggSystem.actorOf(Props[NaiveCrawler], "naive")

    def newEpic[T](work: T) = new Epic[T] { override def iterator = Seq(work).iterator }
    val master = ggSystem.actorOf(StandardCrawler.props, "master")
    val getter = ggSystem.actorOf(Props(new FetchWorker(master)), "worker-1")
    Thread.sleep(1000)
    // Use the code below if you don't care about the result
    // or you want the system to save it for your after retrieval
    // this is where SaveContent actor comes in
    // naive ! newEpic(GET("http://ferrl.in"))
    import scala.concurrent.ExecutionContext.Implicits.global
    import gg.crawler._
    // Use this pattern if you're interest in the result
    // where you're waiting for it before saying it done.
    // val f = naive ? newEpic(GET("http://ferrl.in"))
    (master ? newEpic(Fetch(url = "http://ferrl.in", depth = 1, metadata = List.empty))).onComplete {
      case Success(result) ⇒ println(s"The result is $result")
      case Failure(_) ⇒ // just ignore it.
    }

    Thread.sleep(5000)
    ggSystem.shutdown()
  }
}