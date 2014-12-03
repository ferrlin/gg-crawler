package in.ferrl.crawler.core

import akka.actor.ActorRef
import scala.concurrent.{ Future, future }
import in.ferrl.crawler.pattern.Worker
import in.ferrl.crawler.core.NaiveCrawler._

class ParseWorker(master: ActorRef) extends Worker[ggMessages](master) {
  def isCompatible(someType: ggMessages) = someType match {
    case Parse(_) ⇒ true
    case _ ⇒ false
  }
  def doWork(work: ggMessages): Future[_] = isCompatible(work) match {
    // do nothing for now..
    case true ⇒ Future {}
    case fase ⇒ Future {
      // do nothing
    }
  }
}