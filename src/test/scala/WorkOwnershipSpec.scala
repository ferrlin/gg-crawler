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
  import akka.testkit.TestProbe

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
      val worker1 = TestProbe()
      val actor = system.actorOf(Props[TestWorkOwnership[ggTask]])

      def newEpic[T](work: T) = new Epic[T] { override def iterator = Seq(work).iterator }
      val work = Fetch(url = "http://ferrl.in", depth = 1, metadata = List.empty)

      // default behavior by worker on prestart
      actor ! RegisterWorker(worker1.ref)
      actor ! RequestWorkBy(worker1.ref)

      // actor.expectMsg(500 millis, CurrentlyBusy)

      // send new work
      actor ! newEpic(work)

      // when work is available
      actor ! RequestWorkBy(worker1.ref)

      // if(real.currentEpic)
    }

    "allow us to send work" in {
      val real = TestActorRef[TestWorkOwnership[ggTask]].underlyingActor
      def newEpic[T](work: T) = new Epic[T] { override def iterator = Seq(work).iterator }
      val work = Fetch(url = "http://ferrl.in", depth = 1, metadata = List.empty)
      real.receive(RegisterWorker(testActor))
      real.receive(newEpic(work))
      // TODO: fix this..
      real.currentEpic must be(Some(work))
    }
  }

}