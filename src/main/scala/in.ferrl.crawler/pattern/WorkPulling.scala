package in.ferrl.crawler.pattern

import akka.actor.ActorRef
import scala.collection.IterableLike

object WorkPulling {
  trait Epic[T] extends Iterable[T]

  sealed trait Message
  case class RequestWorkBy(worker: ActorRef) extends Message
  case object CurrentlyBusy extends Message
  case class WorkAvailable[T](someType: T) extends Message
  case class RegisterWorker(worker: ActorRef) extends Message
  case class UnregisterWorker(worker: ActorRef) extends Message
  case class Work[T](work: T) extends Message

  /* custom Messages for this pattern */
  case class Done[T](task: T) extends Message
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

  override def compose: Receive = workHandler orElse workerHandler

  override def workHandler: PartialFunction[Any, Unit] = {
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

  def workerHandler: PartialFunction[Any, Unit] = {
    case RegisterWorker(worker) ⇒
      context.watch(worker)
      workers += worker
      log.info(s"New worker $worker registered")
    case UnregisterWorker(worker) ⇒
      context.unwatch(worker)
      workers.remove(worker)
      log.info(s"Unregistering worker $worker")
    case Terminated(worker) ⇒
      workers.remove(worker)
      log.info(s"Worker $worker died - taking off from worker's pool")
    case RequestWorkBy(worker) ⇒ currentEpic match {
      case None ⇒
        log.info("Worker asked for work but none is available.")
      case Some(epic) ⇒
        val iter = epic.iterator
        if (iter.hasNext) {
          log.info("Send work to requesting worker")
          worker ! Work(iter.next)
        } else {
          log.info(s"Refresh current epic")
          currentEpic = None
        }
    }
  }
}

import scala.concurrent.Future
import scala.reflect.ClassTag

abstract class Worker[T: ClassTag](val master: ActorRef)(implicit manifest: Manifest[T])
  extends Actor with ActorLogging {

  implicit val ec = context.dispatcher

  override def preStart {
    master ! RegisterWorker(self)

    requestWork()
  }

  def requestWork() {
    master ! RequestWorkBy(self)
  }

  def receive = {
    case WorkAvailable(someType: T) ⇒
      if (isCompatible(someType)) requestWork()
    case Work(work: T) ⇒
      doWork(work) onComplete {
        case Success(result) ⇒
          log.info("Work completed successfully.")
          master ! Done(result)
        case Failure(ex) ⇒ log.info(ex.getMessage)
      }
    case Ack ⇒ requestWork()
  }

  def isCompatible(someType: T): Boolean

  def doWork(work: T): Future[Any]
}