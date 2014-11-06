package in.ferrl.crawler.core

import spray.http.HttpEntity
import spray.httpx.unmarshalling.{ MalformedContent, Unmarshaller, Deserialized }

import scala.util.{ Try, Success, Failure }
import java.net.URL
import akka.actor.Actor
import akka.event.Logging
import akka.util.Timeout
import spray.client.pipelining._

import scala.concurrent.duration._
import NaiveCrawler._

/**
 * Companion object for our GetContent actor.
 * Our string unmarshaller is defined here as well
 * as our custom type.
 */
object GetContent {

  private type Elements = List[String]

  /**
   * Constants
   */
  val HTTP = "http"
  val HTTPs = "https"
  val FORWARD_SLASH = "/"
  val HTTP_PREFIX = s"$HTTP://"
  val HTTPs_PREFIX = s"$HTTPs://"
  val JAR_SUFFIX = ".jar"

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
class GetContent extends Actor {

  import GetContent._
  import context.dispatcher

  private val log = Logging(context.system, this)
  implicit val timeout = Timeout(10.seconds)

  private val pipeline = sendReceive ~> unmarshal[Elements]

  def receive = {
    case Request(url) ⇒ request(url.url, Nil)
    case elements: Elements ⇒ // Do nothing for now..
    case _ ⇒ // do nothing for now
  }

  def request(url: String, elements: Elements): Unit = {
    // Validate if url is valid.
    def prepareUrl(url: String): Try[URL] = Try(new URL(url))

    def dropLastSlash(s: String): String = {
      val lastSlash = s.lastIndexOf('/')
      if (lastSlash != -1) s.substring(0, lastSlash) else s
    }

    def processResponse: PartialFunction[Try[Elements], Unit] = {
      case Success(newElements) ⇒
        log.info(s"New elements: $newElements")
        // println(s"New elements: $newElements")
        newElements foreach {
          case element if element startsWith "?" ⇒
          // stay on the same page
          case element if !element.startsWith(FORWARD_SLASH) &&
            !element.startsWith(HTTP) &&
            !element.endsWith(FORWARD_SLASH) ⇒
            self ! elements :+ element
          case element if !element.startsWith(FORWARD_SLASH) &&
            !element.startsWith(HTTP) &&
            element.endsWith(JAR_SUFFIX) ⇒
            // this is .jar file , not a directory
            val (rawVersion :: rawArtifactId :: rawGroupId) = elements.reverse
            val groupId = rawGroupId.reverse.map(dropLastSlash) mkString "."
            val version = dropLastSlash(rawVersion)
            val artifactId = dropLastSlash(rawArtifactId)

          // throws "too many arguments for method " exception
          // dependencyStorage ! (baseUrl, groupId, artifactId, versionId)
          case x ⇒ // some unknown form
        }
      case Failure(ex) ⇒ // do nothing for now
    }

    // 1. Check validity of URL
    prepareUrl(url) match {
      case Success(url) ⇒
        log.info(s"Succeeded crawling for $url")
        // 2. Make an http request to get content
        pipeline(Get(url.toString)) onComplete processResponse
      case Failure(ex) ⇒ log.info(ex.getMessage)
    }
  }
}