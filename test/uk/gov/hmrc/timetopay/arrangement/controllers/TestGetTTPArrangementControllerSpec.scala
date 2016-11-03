package uk.gov.hmrc.timetopay.arrangement.controllers

import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.timetopay.arrangement.connectors.ArrangementDesApiConnector
import uk.gov.hmrc.timetopay.arrangement.models.TTPArrangement
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._
import uk.gov.hmrc.timetopay.arrangement.resources._
import uk.gov.hmrc.timetopay.arrangement.services.{DesTTPArrangementService, LetterAndControlService, TTPArrangementService}

import scala.concurrent.Future


class TestGetTTPArrangementControllerSpec extends UnitSpec with WithFakeApplication{

  "GET /ttparrangements" should {
    "return 200 for arrangement" in {

      object StubbedTTPArrangementService extends TTPArrangementService {
        override def byId(id: String) = {
          Future.successful(Some(ttparrangementResponse.as[TTPArrangement]))
        }
        override val arrangementDesApiConnector = ArrangementDesApiConnector
        override val desTTPArrangementFactory = DesTTPArrangementService
        override val letterAndControlFactory = LetterAndControlService
      }

      object TestController extends TestGetTTPArrangementController {
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
        override def byId(id: String) = {
          Future.successful(None)
        }

      }
      object TestController extends TestGetTTPArrangementController {
        override val arrangementService = StubbedTTPArrangementService
      }

      val fakeRequest = FakeRequest("GET", "/ttparrangements/XXX-XXX-XXX")
      val result = await(TestController.arrangement("XXX-XXX-XXX").apply(fakeRequest))
      status(result) shouldBe Status.NOT_FOUND

    }
  }
}
