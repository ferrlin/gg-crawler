package in.ferrl.crawler.core

import akka.actor.{ Actor, Props }
import akka.util.Timeout
import akka.event.Logging
import in.ferrl.crawler.pattern.{ Master, WorkManager }
import in.ferrl.crawler.pattern.WorkPulling._
import gg.crawler._

class StandardCrawler extends WorkManager[ggTask] with Master[ggTask] {
  import StandardCrawler._

  override def customHandler: Receive = {
    case Done(result, target) ⇒
      currentEpic = None
      target ! WrapUp(result)
      sender ! Ack
    case WrapUp(result) ⇒ result match {
      case ParseComplete(id) ⇒
        log.info("Parsing completed..")
        self ! newEpic(Index(id))
      case FetchComplete(id) ⇒
        log.info("Fetch completed..")
        self ! newEpic(Parse(id))
      case IndexComplete(id) ⇒ log.info("Index completed..")
      case _ ⇒ // do nothing
    }
  }
}

object StandardCrawler {

  def newEpic[T](work: T) = new Epic[T] { override def iterator = Seq(work).iterator }

  def props = Props[StandardCrawler]
}
