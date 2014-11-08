package in.ferrl.crawler.pattern

import akka.actor.{ Actor, ActorRef }
import scala.concurrent.Future
import scala.reflect.ClassTag
import WorkPulling._

abstract class Worker[T: ClassTag](val master: ActorRef)(implicit manifest: Manifest[T])
  extends Actor {

  implicit val ec = context.dispatcher

  override def preStart {
    master ! RegisterWorker(self)
    master ! GimmeWork
  }

  def receive = {
    case WorkAvailable ⇒
      master ! GimmeWork
    case Work(work: T) ⇒
      doWork(work) onComplete { case _ ⇒ master ! GimmeWork }
  }

  def doWork(work: T): Future[_]
}