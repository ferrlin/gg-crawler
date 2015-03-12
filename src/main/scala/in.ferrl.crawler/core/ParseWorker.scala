package in.ferrl.crawler.core

import akka.actor.ActorRef
import scala.concurrent.{ Future, future }
import scala.util.{ Success, Failure }
import in.ferrl.crawler.pattern.Worker
import in.ferrl.crawler.parser.TikaParser
import gg.crawler._

/*class ParseWorker(master: ActorRef) extends Worker[ggTask](master) {

  def isCompatible(someType: ggTask) = someType match {
    case Parse(_) ⇒ true
    case _ ⇒ false
  }

  def doWork(work: ggTask): Future[_] = if (isCompatible(work)) {
    work match {
      case Parse(url,_) ⇒
        parse(url)
        master ! Completed(url)
    }
  }

  import in.ferrl.crawler.dto._
  lazy val parser = new TikaParser

  def parse(url: String): Unit = {
    // where to get content data, from elasticsearch data store?
    // -- parsing logic -- //
    parser.parse(url, content) match {
      case Success(p) ⇒ esDTO.insertParsed(ParsedData(url, p._2))
      case Failure(ex) ⇒ log.error(ex.getMessage)
    }
  }
}*/ 