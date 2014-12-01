package in.ferrl.crawler.core

import akka.actor.{ ActorRef }
import scala.concurrent.{ Future, future }
import in.ferrl.crawler.pattern.Worker
import NaiveCrawler._

class IndexWorker(master: ActorRef) extends Worker[Index](master) {
  def doWork(work: Index): Future[_] = future {
    // do nothing for now..
  }
}
