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
class FetchWorker(master: ActorRef) extends Worker[Task](master) {
  import gg.crawler._
  import FetchWorker._
  import spray.client.pipelining._
  import akka.util.Timeout
  import in.ferrl.crawler.dto._

  implicit val timeout = Timeout(5.seconds)
  def sendAndReceive = sendReceive

  val pipeline: HttpRequest ⇒ Future[Elements] = sendAndReceive ~> unmarshal[Elements]

  def isCompatible(someType: Task): Boolean = someType match {
    case fetch: Fetch ⇒ true
    case _ ⇒ false
  }

  override def customHandler = {
    case task @ Fetch(url, depth, meta) ⇒ {
      def prepareUrl(url: String): Try[URL] = Try(new URL(url))

      def dropLastSlash(s: String): String = {
        val lastSlash = s.lastIndexOf('/')
        if (lastSlash != -1) s.substring(0, lastSlash) else s
      }

      prepareUrl(url) match {
        case Success(url: URL) ⇒
          log.info(s"Fetching commences at url:$url")
          pipeline(Get(url.toString)).onComplete {
            case Success(result) ⇒
              // log.info(s"The result after fetch is -> $result")
              esDTO.insertFetched(FetchedData(url.toString, result))
              master ! Completed(task, "someId", result)
              log.info("Fetching completed successfully.")
            case Failure(e) ⇒ log.error(e.getMessage)
          }
        /*pipeline(Get(url.toString)) flatMap { content ⇒
            esDTO.insertFetched(FetchedData(url.toString, depth, meta, content))
          } onComplete {
            case Success(fetchedId) ⇒
              log.info(s"The result after fetch is -> $fetchedId")
              if (!fetchedId.isEmpty) master ! Complete(f, fetchedId)
            case Failure(err) ⇒
              log.error(err)
              master ! Failed
          }*/
        case Failure(ex) ⇒
          log.error(ex.getMessage)
          master ! Failed
      }
    }
  }
}