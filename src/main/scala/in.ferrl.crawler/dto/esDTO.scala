package in.ferrl.crawler.dto

// case class FetchedData(url: String, meta: List[(String, String)], content: List[String])
case class FetchedData(url: String, content: List[String])
// case class ParsedData(url: String, meta: List[(String, String)])
case class ParsedData(url: String)
// case class IndexedData(url: String, meta: List[(String, String)], content: List[String])
case class IndexedData(url: String, content: List[String])

import argonaut._, Argonaut._
import scala.concurrent.Future

object implicits {
  implicit def FetchedDataEncodeJson: EncodeJson[FetchedData] =
    // jencode3L((f: FetchedData) ⇒ (f.url, f.content))("url", "meta", "content")
    jencode2L((f: FetchedData) ⇒ (f.url, f.content))("url", "content")

  // implicit def MetaDataEncodeJson: EncodeJson[List[(String, String)]] =
  // EncodeJson { case List((k, v)) ⇒ Json.obj(k -> jString(v)) }

  implicit def ParsedDataEncodeJson: EncodeJson[ParsedData] =
    // jencode2L((p: ParsedData) ⇒ (p.url, p.meta))("url", "meta")
    jencode1L((p: ParsedData) ⇒ (p.url))("url")

  implicit def IndexedDataEncodeJson: EncodeJson[IndexedData] =
    // jencode3L((i: IndexedData) ⇒ (i.url, i.meta, i.content))("url", "meta", "content")
    jencode2L((i: IndexedData) ⇒ (i.url, i.content))("url", "content")
}

object esDTO {
  import scala.concurrent.ExecutionContext.Implicits.global
  import in.ferrl.aktic.core.DocPath
  import in.ferrl.aktic.Aktic
  import implicits._

  private[esDTO] val GG_INDEX = "gg"

  val parsedDocPath = DocPath(GG_INDEX, "parsed")
  val fetchedDocPath = DocPath(GG_INDEX, "fetched")
  val indexedDocPath = DocPath(GG_INDEX, "indexed")
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
    println(s"The strJons $strJson")
    client.index(None, strJson)(path).map { res ⇒
      object2IdLens.get(Parse.parseOption(res).get).getOrElse("")
    }
  }
}
