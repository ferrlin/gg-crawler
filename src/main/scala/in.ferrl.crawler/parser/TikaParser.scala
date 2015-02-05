package in.ferrl.crawler.parser

import java.io.ByteArrayInputStream
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.{ AutoDetectParser, ParseContext, Parser }
import org.apache.tika.parser.audio.AudioParser
import org.apache.tika.parser.html.HtmlParser
import org.apache.tika.parser.image.ImageParser
import org.apache.tika.parser.microsoft.OfficeParser
import org.apache.tika.parser.opendocument.OpenOfficeParser
import org.apache.tika.parser.pdf.PDFParser
import org.apache.tika.parser.rtf.RTFParser
import org.apache.tika.parser.txt.TXTParser
import org.apache.tika.parser.xml.XMLParser
import org.apache.tika.sax.WriteOutContentHandler
import scala.util.Try

class TikaParser {
  def parse(url: String, content: String): Try[Pair[String, List[(String, String)]]] = {
    val handler = new WriteOutContentHandler(-1)
    val metadata = new Metadata()
    val context = new ParseContext()
    val parser = getParser(url)

    Try {
      parser.parse(new ByteArrayInputStream(content.getBytes), handler, metadata, context)
      val parsedMetadata = List(
        ("title" -> metadata.get(Metadata.TITLE).toString),
        ("author" -> metadata.get(Metadata.CREATOR).toString))
        .filter { case (k, v) ⇒ v != null }

      Pair(handler.toString(), parsedMetadata)
    }
  }

  def getParser(url: String): Parser = {
    val suffix = url.slice(
      url.lastIndexOf("."), url.length())
    suffix match {
      case "text" | "txt" ⇒ new TXTParser()
      case "html" | "htm" ⇒ new HtmlParser()
      case "xml" ⇒ new XMLParser()
      case "pdf" ⇒ new PDFParser()
      case "rtf" ⇒ new RTFParser()
      case "odt" ⇒ new OpenOfficeParser()
      case "xls" | "xlsx" ⇒ new OfficeParser()
      case "doc" | "docx" ⇒ new OfficeParser()
      case "ppt" | "pptx" ⇒ new OfficeParser()
      case "pst" ⇒ new OfficeParser()
      case "vsd" ⇒ new OfficeParser()
      case "png" ⇒ new ImageParser()
      case "jpg" | "jpeg" ⇒ new ImageParser()
      case "mp3" ⇒ new AudioParser()
      case _ ⇒ new AutoDetectParser()
    }
  }
}