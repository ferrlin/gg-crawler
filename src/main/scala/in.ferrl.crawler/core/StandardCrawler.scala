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
      case Fetch(_, _, _) ⇒
        log.info("Fetch task completed")
        currentEpic = None
      // sender ! Some(result)
      // self ! newEpic(Parse(id))
      case Parse(_) ⇒
        log.info("Parse task completed")
        currentEpic = None
      // self ! newEpic(Parse(id))
      case Index(_) ⇒
        log.info("Index task completed")
        currentEpic = None
      // self ! newEpic(Index(id))
      case _ ⇒ // just ignore other messages
    }
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
