package in.ferrl.crawler.pattern

import akka.actor.ActorRef
import scala.collection.IterableLike

object WorkPulling {
  sealed trait Message
  trait Epic[T] extends Iterable[T]
  case object GimmeWork extends Message
  case object CurrentlyBusy extends Message
  case class WorkAvailable[T](someType: T) extends Message
  case class RegisterWorker(worker: ActorRef) extends Message
  case class Work[T](work: T) extends Message
  /* custom Messages for this pattern */
  case class Done[T](task: T) extends Message
  case object Ack extends Message
}

import akka.actor.{ Actor, Terminated, ActorLogging }
import scala.util.{ Success, Failure }
import scala.collection.mutable

import WorkPulling._

class Master[T] extends Actor with ActorLogging {

  val workers = mutable.Set.empty[ActorRef]
  var currentEpic: Option[Epic[T]] = None

  def receive = {
    case epic: Epic[T] ⇒
      if (currentEpic.isDefined) {
        log.info("Master Actor is busy")
        sender ! CurrentlyBusy
      } else if (workers.isEmpty)
        log.error("Got work but there are no workers registered.")
      else {
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
        log.info("workers asked for work but we've no more work to do")
      case Some(epic) ⇒
        val iter = epic.iterator
        if (iter.hasNext) {
          log.info("Send work to worker from master..")
          sender ! Work(iter.next)
        } else {
          log.info(s"done with current epic $epic")
          currentEpic = None
        }
    }
    case Done(result) ⇒
      currentEpic = None //sender ! Success(result
      wrapUp(result)
      sender ! Ack
  }

  def wrapUp(result: T)
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
          sender ! Done(result)
        case Failure(ex) ⇒ log.info(ex.getMessage)
      }
    case Ack ⇒ master ! GimmeWork
  }

  def isCompatible(someType: T): Boolean

  def doWork(work: T): Future[_]
}