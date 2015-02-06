package in.ferrl.crawler.pattern

import akka.actor.ActorRef
import scala.collection.IterableLike

object WorkPulling {
  trait Epic[T] extends Iterable[T]

  sealed trait Message
  case object GimmeWork extends Message
  case object CurrentlyBusy extends Message
  case class WorkAvailable[T](someType: T) extends Message
  case class RegisterWorker(worker: ActorRef) extends Message
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

trait Master[T] extends Actor with ActorLogging {

  val workers = mutable.Set.empty[ActorRef]
  var currentEpic: Option[Epic[T]] = None

  def receive = coreHandler andThen extendedHandler

  def coreHandler: Receive = {
    case epic: Epic[T] ⇒
      if (currentEpic.isDefined) {
        log.info("Master actor is busy")
        sender ! CurrentlyBusy
      } else if (workers.isEmpty) {
        // When master is initialized, it should have at least one work registered
        log.error("Got work but there are no workers registered.")
      } else {
        currentEpic = Some(epic)
        // workers foreach { _ ! WorkAvailable }
        workers foreach { w ⇒ epic.iterator.foreach { w ! WorkAvailable(_) } }
      }
    case RegisterWorker(worker) ⇒
      log.info(s"worker $worker registered")
      context.watch(worker)
      workers += worker
    case Terminated(worker) ⇒
      log.info(s"worker $worker died - taking off the set of workers")
      workers.remove(worker)
    case GimmeWork ⇒ currentEpic match {
      case None ⇒
        log.info("Worker asked for work but no more work is available.")
      case Some(epic) ⇒
        val iter = epic.iterator
        if (iter.hasNext) {
          log.info("Send work to worker from master..")
          sender ! Work(iter.next)
        } else {
          log.info(s"Done with current epic $epic")
          currentEpic = None
        }
    }
    case Done(result, target) ⇒
      currentEpic = None
      target ! WrapUp(result)
      sender ! Ack
    case WrapUp(result) ⇒ // should be handled by implementing actors
  }

  def extendedHandler: Receive
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