package in.ferrl.crawler.core

import akka.actor.ActorRef
import scala.concurrent.{ Future, future }
import in.ferrl.crawler.pattern.Worker
import in.ferrl.crawler.core.NaiveCrawler._
import in.ferrl.crawler.parser.TikaParser
import NaiveCrawler._

class ParseWorker(master: ActorRef) extends Worker[ggTask](master) {

  def isCompatible(someType: ggTask) = someType match {
    case Parse(_) ⇒ true
    case _ ⇒ false
  }
  def doWork(work: ggTask): Future[_] = if (isCompatible(work)) {
    work match {
      case Parse(url) ⇒
        parse(url)
        ParseComplete(url)
      case _ ⇒ // do nothing
    }
  }
  import con.levinotik.sprastic.client.SprasticClient
  lazy val parser = new TikaParser

  def parse(url: String): Unit = {
    // where to get content data, from elasticsearch data store?
    // -- parsing logic -- //
    parser.parse(url, content) match {
      case Success(meta) ⇒ Sprastic(context) ! Update(url, meta)
      case Failure(ex) ⇒ log.error(ex.getMessage)
    }
  }
}