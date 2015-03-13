package in.ferrl.crawler.parser

import org.jsoup.Jsoup
import in.ferrl.crawler.core.ParseWorker._

class JsoupParser {

  import scala.collection.JavaConversions._
  import argonaut._, Argonaut._

  def parse(json: String): ParsedSchema = {

    lazy val object2UrlLens = jObjectPL >=>
      jsonObjectPL("url") >=>
      jStringPL

    lazy val object2RawContentLens = jObjectPL >=>
      jsonObjectPL("raw") >=>
      jStringPL

    val url = object2UrlLens.get(Parse.parseOption(json).get).getOrElse("")
    val raw = object2RawContentLens.get(Parse.parseOption(json).get).getOrElse("")

    val document = Jsoup.parse(raw)
    val content = Some(document.body().text())
    val links: List[String] = document.select("a").map(_.attr("abs:href")).toList
    val tags: List[String] = document.select("meta").map(_.attr("[property=og:keywords]")).toList
    val desc = Some(document.select("meta[property=og:description]").toString())

    ParsedSchema(url, content, desc, links, tags)
  }
}