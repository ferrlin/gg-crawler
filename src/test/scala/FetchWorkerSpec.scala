package in.ferrl.crawler

class FetchWorkerSpec extends TestKit(ActorSystem("fetch-worker-system"))
  with ImplicitSender
  with Specification
  with Mockito {

  val mockResponse = mock[HttpResponse]
  val mockStatus = mock[StatusCode]
  mockResponse.status returns mockStatus
  mockStatus.isSuccess returns true

  val listElements = Seq(
    "http://ferrl.in",
    "http://github.com/ferrlin",
    "http://blog.ferrl.in",
    "http://www.wikipedia.org")

  val body = HttpEntity(ContentType.`html/text`, listElements.getBytes())
  mockResponse.entity returns body

}