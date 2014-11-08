package in.ferrl.crawler.core

import akka.actor.{ ActorSystem }
import akka.testkit.{ TestActorRef, TestKit, ImplicitSender }
import org.scalatest.{ WordSpecLike, Matchers, BeforeAndAfterAll }

class NaiveCrawlerTest extends TestKit(ActorSystem("gg-crawler-system"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {
  	
}