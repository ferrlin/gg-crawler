package in.ferrl.crawler.core

import akka.actor.{ Actor, Props, ActorLogging }
import akka.util.Timeout
import akka.event.Logging
import in.ferrl.crawler.pattern.WorkManager
import in.ferrl.crawler.pattern.WorkPulling._
import gg.crawler._

class StandardCrawler extends Actor with ActorLogging with WorkManager[ggTask] {
  import StandardCrawler._

  def receive = compose orElse customHandler

  def customHandler: PartialFunction[Any, Unit] = {
    case ParseComplete(id) ⇒
      log.info("Parsing completed..")
      self ! newEpic(Index(id))
      houseKeeping()
    case FetchComplete(id) ⇒
      log.info("Fetch completed..")
      self ! newEpic(Parse(id))
      houseKeeping()
    case IndexComplete(id) ⇒ log.info("Index completed..")
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
