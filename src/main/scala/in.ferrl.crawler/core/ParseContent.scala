package in.ferrl.crawler.core

import akka.actor.ActorRef
import scala.concurrent.{ Future, future }
import in.ferrl.crawler.pattern.Worker
import in.ferrl.crawler.core.NaiveCrawler._

class ParseContent(master: ActorRef) extends Worker[NaiveMsg](master) {

  def doWork(work: NaiveMsg): Future[_] = future {
    // do nothing for now..
  }
}