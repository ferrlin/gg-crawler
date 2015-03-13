package in.ferrl.crawler.core

import akka.actor.ActorRef
import scala.concurrent.{ Future, future }
import scala.util.{ Success, Failure }
import in.ferrl.crawler.pattern.Worker
import in.ferrl.crawler.parser.{ JsoupParser ⇒ HtmlParser }
import gg.crawler._

object ParseWorker {
  case class ParsedSchema(content: Option[String], desc: Option[String], links: List[String], tags: List[String])
}

class ParseWorker(master: ActorRef) extends Worker[Task](master) {

  import ParseWorker._
  import in.ferrl.crawler.dto._

  def isCompatible(someType: Task) = someType match {
    case Parse(_, _) ⇒ true
    case _ ⇒ false
  }

  lazy val htmlParser = new HtmlParser()

  override def customHandler = {
    case task @ Parse(id, _) ⇒
      esDTO.getFetchedDataWith(id) onComplete {
        case Success((url, raw)) ⇒ // parsing the content
          val ParsedSchema(content, desc, links, tags) = htmlParser.parse(raw)
          esDTO.insertParsed(ParsedData(url, content, desc, links, tags)).onComplete {
            case Success(someId) ⇒ master ! Completed(task, someId, None)
            case Failure(e) ⇒ master ! Failed(s"Failed while saving parsed data for $url with error: $e")
          }
        case Failure(e) ⇒
          master ! Failed(s"Failed retrieving raw content from elasticsearch")
      }
  }
}