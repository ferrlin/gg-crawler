package in.ferrl.crawler.core

import akka.actor.{ ActorRef }
import scala.concurrent.{ Future, future }
import in.ferrl.crawler.pattern.Worker
import NaiveCrawler._

class IndexWorker(master: ActorRef) extends Worker[ggMessages](master) {
  def isCompatible(someType: ggMessages) = someType match {
    case Index(_) ⇒ true
    case _ ⇒ false
  }
  def doWork(work: ggMessages): Future[_] = future {
    // do nothing for now..
  }
}
