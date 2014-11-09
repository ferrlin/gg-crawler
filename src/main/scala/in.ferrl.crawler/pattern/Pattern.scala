package in.ferrl.crawler.pattern

import akka.actor.ActorRef
import scala.collection.IterableLike

object WorkPulling {
  sealed trait Message
  trait Epic[T] extends Iterable[T]
  case object GimmeWork extends Message
  case object CurrentlyBusy extends Message
  case object WorkAvailable extends Message
  case class RegisterWorker(worker: ActorRef) extends Message
  case class Work[T](work: T) extends Message
  // custom Message for this pattern
  case class Done[T](task: T) extends Message
}