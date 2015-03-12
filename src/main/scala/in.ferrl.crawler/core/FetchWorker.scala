package in.ferrl.crawler.core

import spray.http.{ HttpEntity, HttpRequest }
import spray.httpx.unmarshalling.{ MalformedContent, Unmarshaller, Deserialized }
import scala.util.{ Try, Success, Failure }
import scala.concurrent.{ Future, future, Promise }
import java.net.URL
import akka.actor.{ Actor, ActorRef }
import scala.concurrent.duration._
import in.ferrl.crawler.pattern.Worker
import in.ferrl.crawler.pattern.WorkPulling._
import gg.crawler._

/**
 * Actor for getting http content
 */
class FetchWorker(master: ActorRef) extends Worker[Task](master) {
  import gg.crawler._
  import spray.client.pipelining._
  import akka.util.Timeout
  import in.ferrl.crawler.dto._

  implicit val timeout = Timeout(5.seconds)
  def sendAndReceive = sendReceive

  val pipeline: HttpRequest ⇒ Future[String] = sendAndReceive ~> unmarshal[String]

  def isCompatible(someType: Task): Boolean = someType match {
    case fetch: Fetch ⇒ true
    case _ ⇒ false
  }

  override def customHandler = {
    case task @ Fetch(url, depth, _) ⇒ {
      def prepareUrl(url: String): Try[URL] = Try(new URL(url))

      def dropLastSlash(s: String): String = {
        val lastSlash = s.lastIndexOf('/')
        if (lastSlash != -1) s.substring(0, lastSlash) else s
      }

      prepareUrl(url) match {
        case Success(url: URL) ⇒
          log.info(s"Fetching commences at url:$url")
          val strUrl = s"$url"
          pipeline(Get(strUrl)).onComplete {
            case Success(result) ⇒
              // log.info(s"The result after fetch is -> $result")
              esDTO.insertFetched(FetchedData(url.toString, Some(result))) onComplete {
                case Success(someId) ⇒ master ! Completed(task, someId, Some(result))
                case Failure(e) ⇒ master ! Failed(s"Failed while saving fetched data for $url with error: $e")
              }
            case Failure(e) ⇒ log.error(e.getMessage)
          }
        case Failure(ex) ⇒
          log.error(ex.getMessage)
          master ! Failed
      }
    }
  }
}