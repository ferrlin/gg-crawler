package in.ferrl.crawler.dto

case class FetchedData(url: String, raw: Option[String])
case class ParsedData(url: String)
case class IndexedData(url: String, content: List[String])

import argonaut._, Argonaut._
import scala.concurrent.Future

object implicits {
  implicit def FetchedDataEncodeJson: EncodeJson[FetchedData] =
    jencode2L((f: FetchedData) ⇒ (f.url, f.raw))("url", "raw")

  implicit def ParsedDataEncodeJson: EncodeJson[ParsedData] =
    jencode1L((p: ParsedData) ⇒ (p.url))("url")

  implicit def IndexedDataEncodeJson: EncodeJson[IndexedData] =
    jencode2L((i: IndexedData) ⇒ (i.url, i.content))("url", "content")
}

import scala.concurrent.ExecutionContext.Implicits.global
import in.ferrl.aktic.core.DocPath

object Const {

  private val GG_INDEX = "gg"
  val parsedDocPath = DocPath(GG_INDEX, "parsed")
  val fetchedDocPath = DocPath(GG_INDEX, "fetched")
  val indexedDocPath = DocPath(GG_INDEX, "indexed")
}

object esDTO {
  import in.ferrl.aktic.Aktic
  import implicits._
  import Const._

  val client = Aktic()
  type ResultId = String

  def insertParsed(parsedData: ParsedData): Future[ResultId] =
    prepare(parsedData.asJson.toString)(parsedDocPath)

  def insertFetched(fetchedData: FetchedData): Future[ResultId] =
    prepare(fetchedData.asJson.toString)(fetchedDocPath)

  def insertIndexed(indexedData: IndexedData): Future[ResultId] =
    prepare(indexedData.asJson.toString)(indexedDocPath)

  lazy val object2IdLens = jObjectPL >=>
    jsonObjectPL("_id") >=>
    jStringPL

  private[this] def prepare(strJson: String)(path: DocPath) = {
    client.index(None, strJson)(path).map { res ⇒
      object2IdLens.get(Parse.parseOption(res).get).getOrElse("")
    }
  }
}
