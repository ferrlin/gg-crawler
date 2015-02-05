package in.ferrl.crawler.core

import spray.http.HttpEntity
import spray.http.HttpRequest
import spray.httpx.unmarshalling.{ MalformedContent, Unmarshaller, Deserialized }
import scala.util.{ Try, Success, Failure }
import scala.concurrent.{ Future, future }
import java.net.URL
import akka.actor.{ Actor, ActorRef }
import scala.concurrent.duration._
import in.ferrl.crawler.pattern.Worker
import in.ferrl.crawler.pattern.WorkPulling._
import gg.crawler._

/**
 * Companion object for our GetContent actor.
 * Our string unmarshaller is defined here as well
 * as our custom type.
 */
object FetchWorker {

  private type Elements = List[String]

  implicit object StringUnmarshaller extends Unmarshaller[Elements] {
    val AHrefRegex = """<a href="([^"]*)">[^<]*</a>""".r
    def apply(entity: HttpEntity): Deserialized[Elements] = {
      val body = entity.asString
      val matches = AHrefRegex.findAllMatchIn(body)
      val hrefs = matches.map(_.group(1)).toList
      Right(hrefs)
    }
  }
}

/**
 * Actor for getting http content
 */
class FetchWorker(master: ActorRef) extends Worker[ggTask](master) {
  import gg.crawler._
  import FetchWorker._
  import spray.client.pipelining._
  import akka.util.Timeout
  import in.ferrl.crawler.dto._

  implicit val timeout = Timeout(5.seconds)
  val pipeline: HttpRequest ⇒ Future[Elements] = sendReceive ~> unmarshal[Elements]

  def isCompatible(someType: ggTask): Boolean = someType match {
    case Fetch(_, _, _) ⇒ true
    case _ ⇒ false
  }

  def doWork(work: ggTask): Future[_] = work match {
    case Fetch(url, depth, meta) ⇒

      def prepareUrl(url: String): Try[URL] = Try(new URL(url))

      def dropLastSlash(s: String): String = {
        val lastSlash = s.lastIndexOf('/')
        if (lastSlash != -1) s.substring(0, lastSlash) else s
      }

      prepareUrl(url) match {
        case Success(url: URL) ⇒
          log.info(s"Requesting to get content of $url..")
          val content = pipeline(Get(url.toString))
          // Let's just ignore the response
          // content map {
          esDTO.insertFetched(FetchedData(url.toString, depth, meta, _))
        // }
        // save the content to elastic search
        // val Promise(id) = SprasticContext(context) ? Add(url, depth, meta, content)
        // master ! FetchComplete(id)
        case Failure(ex) ⇒ future { log.error(ex.getMessage) }
      }
    case _ ⇒ future {
      // do nothing
    }
  }
}