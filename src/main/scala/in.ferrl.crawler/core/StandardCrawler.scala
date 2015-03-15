package in.ferrl.crawler.core

import akka.actor.{ Actor, Props, ActorLogging }
import akka.util.Timeout
import akka.event.Logging
import in.ferrl.crawler.pattern.WorkManager
import in.ferrl.crawler.pattern.WorkPulling._
import gg.crawler._

class StandardCrawler extends Actor with ActorLogging with WorkManager[Task] {
  import StandardCrawler._

  def receive = compose orElse customHandler

  def customHandler: PartialFunction[Any, Unit] = {
    case Completed(task, id, result) ⇒ task match {
      case Fetch(_, _, proceed) ⇒
        log.info("Fetch completed successfully.")
        currentEpic = None
        zender.get ! Some(result)
        if (proceed) self ! newEpic(Parse(id, proceed))
      case Parse(_, proceed) ⇒
        log.info("Parse completed successfully.")
        currentEpic = None
        // if (proceed) self ! newEpic(Index(id))
      case Index(_) ⇒
        log.info("Index completed successfully.")
        currentEpic = None
      case _ ⇒ // just ignore other messages
    }
    case Failed(message) ⇒ log.warning(message)
  }

  def houseKeeping() {
    currentEpic = None
    sender ! Ack
  }
}

object StandardCrawler {

  def newEpic[T](work: T) = new Epic[T] { override def iterator = Seq(work).iterator }

  def props = Props[StandardCrawler]
}
