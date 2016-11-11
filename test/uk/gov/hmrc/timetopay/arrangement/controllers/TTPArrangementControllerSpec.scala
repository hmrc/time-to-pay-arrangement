package uk.gov.hmrc.timetopay.arrangement.controllers

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.timetopay.arrangement.connectors.ArrangementDesApiConnector
import uk.gov.hmrc.timetopay.arrangement.models.TTPArrangement
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._
import uk.gov.hmrc.timetopay.arrangement.repositories.TTPArrangementRepository
import uk.gov.hmrc.timetopay.arrangement.resources._
import uk.gov.hmrc.timetopay.arrangement.services.{LetterAndControlService, DesTTPArrangementService, TTPArrangementService}

import scala.concurrent.Future

class TTPArrangementControllerSpec extends UnitSpec with MockFactory with ScalaFutures {

  class MockService extends TTPArrangementService(null,null,null,null, null) {}

  val arrangementServiceStub = stub[MockService]
  val arrangementController = new TTPArrangementController(arrangementServiceStub)

  "POST /ttparrangements" should {
    "return 201" in {

      implicit val hc = HeaderCarrier
      (arrangementServiceStub.submit(_: TTPArrangement)(_: HeaderCarrier)).when(ttparrangementRequest.as[TTPArrangement], *) returns Some(ttparrangementResponse.as[TTPArrangement])

      val fakeRequest = FakeRequest("POST", "/ttparrangements").withBody(ttparrangementRequest)
      val result = arrangementController.create().apply(fakeRequest).futureValue
      status(result) shouldBe Status.CREATED
      result.header.headers.get("Location") should not be null
    }

    "return 500 if arrangement service fails" in {
      implicit val hc = HeaderCarrier
      (arrangementServiceStub.submit(_: TTPArrangement)(_: HeaderCarrier)).when(ttparrangementRequest.as[TTPArrangement], *) returns Future.failed(new RuntimeException("DES API Submission Failed"))

      val fakeRequest = FakeRequest("POST", "/ttparrangements").withBody(ttparrangementRequest)
      val result = arrangementController.create().apply(fakeRequest).futureValue
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

  }

  "GET /ttparrangements" should {
    "return 200 for arrangement" in {

      (arrangementServiceStub.byId(_: String)).when("XXX-XXX-XXX") returns Future.successful(Some(ttparrangementResponse.as[TTPArrangement]))

      val fakeRequest = FakeRequest("GET", "/ttparrangements/XXX-XXX-XXX")
      val result = arrangementController.arrangement("XXX-XXX-XXX").apply(fakeRequest).futureValue
      status(result) shouldBe Status.OK
      Json.parse(bodyOf(result)).as[TTPArrangement].desArrangement.get.ttpArrangement.saNote shouldBe "SA Note Text Here"
    }


    "return 404 for non existent arrangement" in {

      (arrangementServiceStub.byId(_: String)).when("XXX-XXX-XXX") returns Future.successful(None)

      val fakeRequest = FakeRequest("GET", "/ttparrangements/XXX-XXX-XXX")
      val result = arrangementController.arrangement("XXX-XXX-XXX").apply(fakeRequest).futureValue
      status(result) shouldBe Status.NOT_FOUND

    }
  }


}
