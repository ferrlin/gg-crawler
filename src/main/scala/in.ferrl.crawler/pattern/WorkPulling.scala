package in.ferrl.crawler.pattern

import akka.actor.ActorRef
import scala.collection.IterableLike

object WorkPulling {
  trait Epic[T] extends Iterable[T]

  sealed trait Message
  case object GimmeWork extends Message
  // RequestWork to change GimmeWork 
  case object RequestWork extends Message
  case object CurrentlyBusy extends Message
  case class WorkAvailable[T](someType: T) extends Message
  case class RegisterWorker(worker: ActorRef) extends Message
  case class UnregisterWorker(worker: ActorRef) extends Message
  case class Work[T](work: T) extends Message

  /* custom Messages for this pattern */
  case class Done[T](task: T, target: ActorRef) extends Message
  case class WrapUp[T](task: T) extends Message
  case object Ack extends Message
}

import akka.actor.{ Actor, Terminated, ActorLogging }
import scala.util.{ Success, Failure }
import scala.collection.mutable
import WorkPulling._

trait WorkOwnership[T] {
  var currentEpic: Option[Epic[T]] = None

  def workHandler: Actor.Receive

  def compose: Actor.Receive
}

trait WorkManager[T] extends WorkOwnership[T] { this: Actor with ActorLogging ⇒
  val workers = mutable.Set.empty[ActorRef]

  override def compose = workHandler orElse workerHandler

  override def workHandler: Receive = {
    case epic: Epic[T] ⇒
      if (currentEpic.isDefined) {
        log.info("Master is busy.")
        sender ! CurrentlyBusy
      } else if (workers.isEmpty) {
        log.error("Work is available but no workers are registered.")
      } else {
        currentEpic = Some(epic)
        workers foreach { w ⇒ epic.iterator.foreach { w ! WorkAvailable(_) } }
      }
  }

  def workerHandler: Receive = {
    case RegisterWorker(worker) ⇒
      log.info(s"New worker $worker registered")
      context.watch(worker)
      workers += worker
    case UnregisterWorker(worker) ⇒
      log.info(s"Unregistering worker $worker")
      context.unwatch(worker)
      workers.remove(worker)
    case Terminated(worker) ⇒
      log.info(s"Worker $worker died - taking off from worker's pool")
      workers.remove(worker)
    case RequestWork ⇒ currentEpic match {
      case None ⇒
        log.info("Worker asked for work but none is available.")
      case Some(epic) ⇒
        val iter = epic.iterator
        if (iter.hasNext) {
          log.info("Send work to requesting worker")
          sender ! Work(iter.next)
        } else {
          log.info(s"Refresh current epic")
          currentEpic = None
        }
    }
  }

}

trait Master[T] extends Actor with ActorLogging { this: WorkOwnership[T] ⇒

  def receive = compose andThen customHandler

  def customHandler: Receive
}

import scala.concurrent.Future
import scala.reflect.ClassTag

abstract class Worker[T: ClassTag](val master: ActorRef)(implicit manifest: Manifest[T])
  extends Actor with ActorLogging {

  implicit val ec = context.dispatcher

  override def preStart {
    master ! RegisterWorker(self)
    master ! GimmeWork
  }

  def receive = {
    case WorkAvailable(someType: T) ⇒
      if (isCompatible(someType)) master ! GimmeWork
    case Work(work: T) ⇒
      doWork(work) onComplete {
        case Success(result) ⇒
          log.info("Work completed successfully.")
          sender ! Done(result, master)
        case Failure(ex) ⇒ log.info(ex.getMessage)
      }
    case Ack ⇒ master ! GimmeWork
  }

  def isCompatible(someType: T): Boolean

  def doWork(work: T): Future[Any]
}