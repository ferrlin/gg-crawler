package in.ferrl.crawler.core

import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import org.scalatest.Matchers

import com.typesafe.config.ConfigFactory
import akka.actor.{ Actor, ActorSystem, ActorRef, Props }
import akka.testkit.{ TestKit, TestActorRef, DefaultTimeout, ImplicitSender, TestActors }
import scala.concurrent.duration._

class CrawlerUsageSpec extends TestKit(ActorSystem("ggSystem",
  ConfigFactory.parseString(CrawlerUsageSpec.config)))
  with DefaultTimeout with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

	 val master = system.actorOf(Props(classOf[NaiveCrawler], testActor))
  val gcWorker1 = system.actorOf(Props(new GetContent(master)), "worker-1")

}

object CrawlerUsageSpec {
  // Define your test specific configuration here
  val config = """
  	akka {
  		loglevel = "WARNING"
  	}
  	"""

}