package uk.gov.hmrc.timetopay.arrangement

import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.timetopay.arrangement.connectors.ArrangementDesApiConnector
import uk.gov.hmrc.timetopay.arrangement.controllers.TTPArrangementController
import uk.gov.hmrc.timetopay.arrangement.models.TTPArrangement
import uk.gov.hmrc.timetopay.arrangement.services.TTPArrangementService

import scala.concurrent.Future
import scala.io.Source
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._

class TTPArrangementControllerSpec extends UnitSpec with WithFakeApplication {

  val source: String = Source.fromInputStream(getClass.getResourceAsStream("/ttparrangement-request.json")).getLines.mkString
  val json: JsValue = Json.parse(source)


  "POST /ttparrangements" should {
    "return 201" in {

      val fakeRequest = FakeRequest("POST", "/ttparrangements").withBody(json)
      val result = TTPArrangementController.create().apply(fakeRequest)
      status(result) shouldBe Status.CREATED
      await(result).header.headers.get("Location") should not be null
    }

    "return 500 if arrangement service fails" in {
      object StubbedTTPArrangementService extends TTPArrangementService {
        override def submit(arrangement: TTPArrangement)  = {
          Future.failed(new RuntimeException("DES API Submission Failed"))
        }

        override val arrangementDesApiConnector = ArrangementDesApiConnector
      }

      object TTPTestArrangementController extends TTPArrangementController {
        override val arrangementService = StubbedTTPArrangementService
      }

      val fakeRequest = FakeRequest("POST", "/ttparrangements").withBody(json)
      val result = await(TTPTestArrangementController.create().apply(fakeRequest))
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

  }


  "GET /ttparrangements" should {
    "return 200 for arrangement" in {
      val responseArrangement: String = Source.fromInputStream(getClass.getResourceAsStream("/ttaparrangement-response.json")).getLines.mkString
      val jsonArrangement: JsValue = Json.parse(responseArrangement)

      object StubbedTTPArrangementService extends TTPArrangementService {
        override def byId(id: String)  = {
          Future.successful(Some(jsonArrangement.as[TTPArrangement]))
        }
        override val arrangementDesApiConnector = ArrangementDesApiConnector
      }

      object TTPTestArrangementController extends TTPArrangementController {
        override val arrangementService = StubbedTTPArrangementService
      }

      val fakeRequest = FakeRequest("GET", "/ttparrangements/XXX-XXX-XXX")
      val result = await(TTPTestArrangementController.arrangement("XXX-XXX-XXX").apply(fakeRequest))
      status(result) shouldBe Status.OK
      Json.parse(bodyOf(result)).as[TTPArrangement].ttpArrangement.get.saNote shouldBe "SA Note Text Here"
    }

    "return 404 for non existent arrangement" in {
      object StubbedTTPArrangementService extends TTPArrangementService {
        override def byId(id: String)  = {
          Future.successful(None)
        }
        override val arrangementDesApiConnector = ArrangementDesApiConnector
      }

      object TTPTestArrangementController extends TTPArrangementController {
        override val arrangementService = StubbedTTPArrangementService
      }

      val fakeRequest = FakeRequest("GET", "/ttparrangements/XXX-XXX-XXX")
      val result = await(TTPTestArrangementController.arrangement("XXX-XXX-XXX").apply(fakeRequest))
      status(result) shouldBe Status.NOT_FOUND

    }
  }

}
