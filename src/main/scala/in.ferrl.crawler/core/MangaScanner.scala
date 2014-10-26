package in.ferrl.crawler.core

import akka.util.Timeout
import akka.event.Logging
import akka.actor.{ Actor, ActorRef, ActorSystem }
import spray.httpx.unmarshalling.{ MalformedContent, Unmarshaller, Deserialized }
import spray.http.HttpEntity
import spray.client.pipelining._
import scala.util.{ Try, Success, Failure }

object WebCrawler {

  // Constants 
  val HTTP = "http"
  val HTTPs = "https"
  val FORWARD_SLASH = "/"
  val HTTP_PREFIX = s"$HTTP://"
  val HTTPs_PREFIX = s"$HTTPs://"
  val JAR_SUFFIX = ".jar"

  // Messages
  case class Crawl(targetUrl: String)

  private type Elements = List[String]

  implicit object StringUnmarshaller extends Unmarshaller[Elements] {
    val AHrefRegex = """&lt;a href="([^"]*)"&gt;[^&lt;]*&lt;/a&gt;""".r
    def apply(entity: HttpEntity): Deserialized[Elements] = {
      val body = entity.asString
      val matches = AHrefRegex.findAllMatchIn(body)
      val hrefs = matches.map(_.group(1)).toList
      Right(hrefs)
    }
  }
}

class Crawler(parserActor: ActorRef) extends Actor {
	import WebCrawler._

	def receive: Receive = {
		case Crawl(targetUrl) =>
			require(targetUrl.endsWith(FORWARD_SLASH), "The url must end with /")
			require(targetUrl.startsWith(HTTP_PREFIX) ||
				targetUrl.startsWith(HTTPs_PREFIX),
				"The URL must start with http:// or https://")
	}
}

class WebCrawler(baseUrl: String, dependencyStorage: ActorRef) extends Actor {
  import WebCrawler._
  require(baseUrl.endsWith(FORWARD_SLASH), "The url must end with /")
  require(baseUrl.startsWith(HTTP_PREFIX) || baseUrl.startsWith(HTTPs_PREFIX),
    "The URL must start with http:// or https://")

  import scala.concurrent.duration._
  import context.dispatcher

  val log = Logging(context.system, this)

  def receive: Receive = {
    case Crawl(target) => {
      // log.debug("Crawl message received. Initiating..")
      println("Crawl message received. Initiating..")
      descend(Nil)// Do nothing for now..
    }
    case elements: Elements => // Do nothing for now..
  }

  private implicit val timeout = Timeout(10.seconds)

  private val pipeline = sendReceive ~> unmarshal[Elements]

  def descend(elements: Elements): Unit = {
    def prepareUrl(elements: Elements): String =
      baseUrl + elements.mkString("")

    def dropLastSlash(s: String): String = {
      val lastSlash = s.lastIndexOf('/')
      if (lastSlash != -1) s.substring(0, lastSlash) else s
    }

    def processResponse: PartialFunction[Try[Elements], Unit] = {
      case Success(newElements) =>
        println(s"New elements: $newElements")
        newElements foreach {
          case element if element startsWith "?" =>
          // stay on the same page
          case element if !element.startsWith(FORWARD_SLASH) &&
            !element.startsWith(HTTP) &&
            !element.endsWith(FORWARD_SLASH) =>
            self ! elements :+ element
          case element if !element.startsWith(FORWARD_SLASH) &&
            !element.startsWith(HTTP) &&
            element.endsWith(JAR_SUFFIX) =>
            // this is .jar file , not a directory
            val (rawVersion :: rawArtifactId :: rawGroupId) = elements.reverse
            val groupId = rawGroupId.reverse.map(dropLastSlash) mkString "."
            val version = dropLastSlash(rawVersion)
            val artifactId = dropLastSlash(rawArtifactId)

          // throws "too many arguments for method " exception
          // dependencyStorage ! (baseUrl, groupId, artifactId, versionId)
          case x => // some unknown form
        }
      case Failure(exception) => // report scan error
    }

    val url = prepareUrl(elements)
    pipeline(Get(url)) onComplete processResponse
  }
}