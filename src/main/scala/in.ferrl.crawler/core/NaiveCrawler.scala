package in.ferrl.crawler.core

import akka.actor.{ Actor, Props }
import akka.util.Timeout
import akka.event.Logging
import in.ferrl.crawler.pattern.Master
import in.ferrl.crawler.pattern.Master._
import gg.crawler._

class NaiveCrawler extends Master[ggTask] {

  def newEpic[T](work: T) = new Epic[T] { override def iterator = Seq(work).iterator }

  def wrapUp(result: ggTask) = {
    result match {
      case ParseComplete(id) ⇒
        log.info("Parsing completed..")
        self ! newEpic(Index(id))
      case FetchComplete(id) ⇒
        log.info("Fetch completed..")
        self ! newEpic(Parse(id))
      case IndexComplete(id) ⇒ log.info("Index completed..")
      case _ => // do nothing
    }
  }
}
