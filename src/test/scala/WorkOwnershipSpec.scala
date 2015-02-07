package in.ferrl.crawler

import akka.actor.{ Props, Actor, ActorSystem, ActorLogging }
import akka.testkit.{ TestKit, TestActorRef, ImplicitSender }
import org.scalatest.{ WordSpecLike, BeforeAndAfterAll, MustMatchers }

import gg.crawler._
import in.ferrl.crawler.pattern._

class TestWorkOwnershipWithWorkerHandler extends Actor with ActorLogging with WorkManager[ggTask] {
  def receive = workerHandler
}

class TestWorkOwnership[T] extends Actor with ActorLogging with WorkManager[T] {
  def receive = workHandler orElse workerHandler
}

class WorkOwnershipSpec extends TestKit(ActorSystem("WorkOwnershipSpec"))
  with ImplicitSender
  with WordSpecLike
  with MustMatchers
  with BeforeAndAfterAll {

  import in.ferrl.crawler.pattern.WorkPulling._

  "WorkOwnership" should {
    "allow us to register a worker" in {
      val real = TestActorRef[TestWorkOwnershipWithWorkerHandler].underlyingActor
      real.receive(RegisterWorker(testActor))
      real.workers must contain(testActor)
    }
    "allow us to unregister a worker" in {
      val real = TestActorRef[TestWorkOwnershipWithWorkerHandler].underlyingActor
      real.receive(RegisterWorker(testActor))
      real.receive(UnregisterWorker(testActor))
      real.workers.size must be(0)
    }
    // This should be in the integration test
    "allows worker to request work" in {
      val real = TestActorRef[TestWorkOwnershipWithWorkerHandler].underlyingActor
      real.receive(RequestWork)

      // if(real.currentEpic)
      // TODO:how to test ..
    }
    "allow us to send work" in {
      val real = TestActorRef[TestWorkOwnership[ggTask]].underlyingActor
      def newEpic[T](work: T) = new Epic[T] { override def iterator = Seq(work).iterator }
      val work = Fetch(url = "http://ferrl.in", depth = 1, metadata = List.empty)
      real.receive(RegisterWorker(testActor))
      real.receive(newEpic(work))
      // TODO: fix this..
      // real.currentEpic must be(Some(work))
    }
  }

}