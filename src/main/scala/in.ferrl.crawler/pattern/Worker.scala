package in.ferrl.crawler.pattern

import akka.actor.{ Actor, ActorRef }
import akka.event.Logging
import scala.concurrent.Future
import scala.util.Success
import scala.reflect.ClassTag
import WorkPulling._

abstract class Worker[T: ClassTag](val master: ActorRef)(implicit manifest: Manifest[T])
  extends Actor {

  val log = Logging(context.system, this)
  implicit val ec = context.dispatcher

  override def preStart {
    master ! RegisterWorker(self)
    master ! GimmeWork
  }

  def receive = {
    case WorkAvailable ⇒
      master ! GimmeWork
    case Work(work: T) ⇒
      doWork(work) onComplete {
        case Success(result) ⇒
          log.info(s"Result:$result")
          master ! GimmeWork
        case _ ⇒ master ! GimmeWork
      }
  }

  def doWork(work: T): Future[_]
}