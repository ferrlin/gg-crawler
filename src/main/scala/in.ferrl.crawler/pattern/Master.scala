package in.ferrl.crawler.pattern

import akka.actor.{ Actor, ActorRef, Terminated }
import scala.collection.mutable
import akka.event.Logging
import WorkPulling._

class Master[T] extends Actor {

  val log = Logging(context.system, this)
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
        workers foreach { _ ! WorkAvailable }
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
    case Done(result) ⇒ sender ! result
  }
}
