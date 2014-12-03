package in.ferrl.crawler.core

import akka.actor.ActorRef
import scala.concurrent.{ Future, future }
import in.ferrl.crawler.pattern.Worker
import in.ferrl.crawler.core.NaiveCrawler._

class ParseWorker(master: ActorRef) extends Worker[ggTask](master) {
  def isCompatible(someType: ggTask) = someType match {
    case Parse(_) ⇒ true
    case _ ⇒ false
  }
  def doWork(work: ggTask): Future[_] = isCompatible(work) match {
    // do nothing for now..
    case true ⇒ Future {}
    case false ⇒ Future {
      // do nothing
    }
  }
}