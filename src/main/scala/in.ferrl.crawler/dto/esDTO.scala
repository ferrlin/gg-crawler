package in.ferrl.crawler.dto

case class FetchedData(url: String, depth: Int, meta: List[(String, String)], content: List[String])
case class ParsedData(url: String, meta: List[(String, String)])
case class IndexedData(url: String, meta: List[(String, String)], content: List[String])

import argonaut._, Argonaut._
import scala.concurrent.Future

object implicits {
  implicit def FetchedDataEncodeJson: EncodeJson[FetchedData] =
    jencode4L((f: FetchedData) ⇒ (f.url, f.depth, f.meta, f.content))("url", "depth", "meta", "content")

  implicit def MetaDataEncodeJson: EncodeJson[List[(String, String)]] =
    EncodeJson { case List((k, v)) ⇒ Json.obj(k -> v) }

  implicit def ParsedDataEncodeJson: EncodeJson[ParsedData] =
    jencode2L((p: ParsedData) ⇒ (p.url, p.meta))("url", "meta")

  implicit def IndexedDataEncodeJson: EncodeJson[IndexedData] =
    jencode3L((i: IndexedData) ⇒ (i.url, i.meta, i.content))("url", "meta", "content")
}

object esDTO {
  import in.ferrl.aktic.core.DocPath
  import in.ferrl.aktic.Aktic
  import implicits._

  private val GG_INDEX = "gg"

  val parsedDocPath = DocPath(GG_INDEX, "parsed")
  val fetchedDocPath = DocPath(GG_INDEX, "fetched")
  val indexedDocPath = DocPath(GG_INDEX, "indexed")

  val client = Aktic()

  def insertParsed(parsedData: ParsedData): Future[String] =
    prepare(parsedData.asJson.toString)(parsedDocPath)

  def insertFetched(fetchedData: FetchedData): Future[String] =
    prepare(fetchedData.asJson.toString)(fetchedDocPath)

  def insertIndexed(indexedData: IndexedData): Future[String] =
    prepare(indexedData.asJson.toString)(indexedDocPath)

  private[this] def prepare(strJson: String)(path: DocPath) = {
    client.index(None, strJson)(path)
  }
}
