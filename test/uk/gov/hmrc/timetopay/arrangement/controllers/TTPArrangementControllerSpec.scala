package uk.gov.hmrc.timetopay.arrangement.controllers

import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.timetopay.arrangement.connectors.ArrangementDesApiConnector
import uk.gov.hmrc.timetopay.arrangement.models.TTPArrangement
import uk.gov.hmrc.timetopay.arrangement.resources._
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._
import uk.gov.hmrc.timetopay.arrangement.services.{DesTTPArrangementService, LetterAndControlService, TTPArrangementService}

import scala.concurrent.Future

class TTPArrangementControllerSpec extends UnitSpec with WithFakeApplication {


  "POST /ttparrangements" should {
    "return 201" in {
      object StubbedTTPArrangementService extends TTPArrangementService {
        override val arrangementDesApiConnector = ArrangementDesApiConnector
        override val desTTPArrangementFactory = DesTTPArrangementService
        override val letterAndControlFactory = LetterAndControlService

        override def submit(arrangement: TTPArrangement)(implicit hc: HeaderCarrier) = {
          Future.successful(ttparrangementResponse.as[TTPArrangement])
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


}
