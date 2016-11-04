package uk.gov.hmrc.timetopay.arrangement.controllers

import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.timetopay.arrangement.connectors.ArrangementDesApiConnector
import uk.gov.hmrc.timetopay.arrangement.models.TTPArrangement
import uk.gov.hmrc.timetopay.arrangement.resources._
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._
import uk.gov.hmrc.timetopay.arrangement.repositories.TTPArrangementRepository
import uk.gov.hmrc.timetopay.arrangement.services.{DesTTPArrangementService, LetterAndControlService, TTPArrangementService}

import scala.concurrent.Future

class TTPArrangementControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication {


  "POST /ttparrangements" should {
    "return 201" in {
      object StubbedTTPArrangementService extends TTPArrangementService {
        override val arrangementDesApiConnector = ArrangementDesApiConnector
        override val desTTPArrangementFactory = DesTTPArrangementService
        override val letterAndControlFactory = LetterAndControlService
        override val ttpArrangementRepository: TTPArrangementRepository = mock[TTPArrangementRepository]


        override def submit(arrangement: TTPArrangement)(implicit hc: HeaderCarrier) = {
          Future.successful(Some(ttparrangementResponse.as[TTPArrangement]))
        }

      }

      object TTPTestArrangementController extends TTPArrangementController {
        override val arrangementService = StubbedTTPArrangementService
      }

      val fakeRequest = FakeRequest("POST", "/ttparrangements").withBody(ttparrangementRequest)
      val result = TTPTestArrangementController.create().apply(fakeRequest)
      status(result) shouldBe Status.CREATED
      await(result).header.headers.get("Location") should not be null
    }

    "return 500 if arrangement service fails" in {
      object StubbedTTPArrangementService extends TTPArrangementService {
        override val arrangementDesApiConnector = ArrangementDesApiConnector
        override val desTTPArrangementFactory = DesTTPArrangementService
        override val letterAndControlFactory = LetterAndControlService
        override val ttpArrangementRepository: TTPArrangementRepository = mock[TTPArrangementRepository]

        override def submit(arrangement: TTPArrangement)(implicit hc: HeaderCarrier) = {
          Future.failed(new RuntimeException("DES API Submission Failed"))
        }

      }

      object TTPTestArrangementController extends TTPArrangementController {
        override val arrangementService = StubbedTTPArrangementService
      }

      val fakeRequest = FakeRequest("POST", "/ttparrangements").withBody(ttparrangementRequest)
      val result = await(TTPTestArrangementController.create().apply(fakeRequest))
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

  }

  "GET /ttparrangements" should {
    "return 200 for arrangement" in {

      object StubbedTTPArrangementService extends TTPArrangementService {
        override def byId(id: String) = {
          Future.successful(Some(ttparrangementResponse.as[TTPArrangement]))
        }
        override val arrangementDesApiConnector = ArrangementDesApiConnector
        override val desTTPArrangementFactory = DesTTPArrangementService
        override val letterAndControlFactory = LetterAndControlService
        override val ttpArrangementRepository: TTPArrangementRepository = mock[TTPArrangementRepository]

      }

      object TestController extends TTPArrangementController {
        override val arrangementService = StubbedTTPArrangementService
      }

      val fakeRequest = FakeRequest("GET", "/ttparrangements/XXX-XXX-XXX")
      val result = await(TestController.arrangement("XXX-XXX-XXX").apply(fakeRequest))
      status(result) shouldBe Status.OK
      Json.parse(bodyOf(result)).as[TTPArrangement].desArrangement.get.ttpArrangement.saNote shouldBe "SA Note Text Here"
    }


    "return 404 for non existent arrangement" in {
      object StubbedTTPArrangementService extends TTPArrangementService {
        override val desTTPArrangementFactory = DesTTPArrangementService
        override val letterAndControlFactory = LetterAndControlService
        override val arrangementDesApiConnector = ArrangementDesApiConnector
        override val ttpArrangementRepository: TTPArrangementRepository = mock[TTPArrangementRepository]

        override def byId(id: String) = {
          Future.successful(None)
        }

      }
      object TestController extends TTPArrangementController {
        override val arrangementService = StubbedTTPArrangementService
      }

      val fakeRequest = FakeRequest("GET", "/ttparrangements/XXX-XXX-XXX")
      val result = await(TestController.arrangement("XXX-XXX-XXX").apply(fakeRequest))
      status(result) shouldBe Status.NOT_FOUND

    }
  }


}
