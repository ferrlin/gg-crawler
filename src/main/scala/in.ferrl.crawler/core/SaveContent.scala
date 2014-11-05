package in.ferrl.crawler.core

import akka.actor.Actor
import NaiveCrawler._
/**
 * Actor for saving content to external data store
 * ie elastic search, cassandra
 */
class SaveContent extends Actor {
  def receive = {
    case Save ⇒ // do nothing for now..
    case _ ⇒ // do nothing for now..
  }
}
