package in.ferrl.crawler.core

import akka.actor.ActorRef
import scala.concurrent.{ Future, future }
import in.ferrl.crawler.pattern.Worker
import in.ferrl.crawler.core.NaiveCrawler._

class ParseWorker(master: ActorRef) extends Worker[Parse](master) {
  def doWork(work: Parse): Future[_] = future {
    // do nothing for now..
  }
}