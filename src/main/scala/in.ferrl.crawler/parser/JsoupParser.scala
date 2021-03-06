package in.ferrl.crawler.parser

import org.jsoup.Jsoup
import in.ferrl.crawler.core.ParseWorker._

class JsoupParser {

  import scala.collection.JavaConversions._
  import argonaut._, Argonaut._

  def parse(raw: String): ParsedSchema = {

    val document = Jsoup.parse(raw)
    val content = Some(document.body().text())
    val links: List[String] = document.select("a").map(_.attr("abs:href")).toList
    val tags: List[String] = document.select("meta").map(_.attr("[property=og:keywords]")).toList
    val desc = Some(document.select("meta[property=og:description]").toString())
    
    // val content = Some("THis is a testing content")
    // val links = List("http://ferrl.in", "http://www.example.com")
    // val tags = List("test", "sample", "mock")
    // val desc = Some("This is a sample description")

    new ParsedSchema(content, desc, links, tags)
  }
}