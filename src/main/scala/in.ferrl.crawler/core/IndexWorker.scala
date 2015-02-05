package in.ferrl.crawler.core

import akka.actor.{ ActorRef }
import scala.concurrent.{ Future, future }
import in.ferrl.crawler.pattern.Worker
import gg.crawler._

/*class IndexWorker(master: ActorRef) extends Worker[ggTask](master) {
  def isCompatible(someType: ggTask) = someType match {
    case Index(_) ⇒ true
    case _ ⇒ false
  }
  def doWork(work: ggTask): Future[_] = future {
    // do nothing for now..
  }
}
*/ 