package uk.gov.hmrc.timetopay.arrangement.services

import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.timetopay.arrangement.connectors.ArrangementDesApiConnector
import uk.gov.hmrc.timetopay.arrangement.models.TTPArrangement
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._
import uk.gov.hmrc.timetopay.arrangement.resources._
import org.mockito.Matchers._
import uk.gov.hmrc.timetopay.arrangement.repositories.TTPArrangementRepository

import scala.concurrent.Future

class TTPArrangementServiceSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  "TTPArrangementService" should {
    "return Future[Success]" in {
      object TestArrangementService extends TTPArrangementService {
        override val arrangementDesApiConnector = mock[ArrangementDesApiConnector]
        override val desTTPArrangementService = DesTTPArrangementService
        override val letterAndControlService = LetterAndControlService
        override val ttpArrangementRepository: TTPArrangementRepository = mock[TTPArrangementRepository]
      }

      val arrangement: TTPArrangement = ttparrangementRequest.as[TTPArrangement]
      val savedArrangement = ttparrangementResponse.as[TTPArrangement]

      when(TestArrangementService.ttpArrangementRepository.save(any)).thenReturn(Future.successful(Some(savedArrangement)))
      when(TestArrangementService.arrangementDesApiConnector.submitArrangement(any(), any())(any())).thenReturn(Future.successful(true))
      val headerCarrier = new HeaderCarrier
      val response = TestArrangementService.submit(arrangement)(headerCarrier)

      ScalaFutures.whenReady(response) { r =>
        val desSubmissionRequest = r.get.desArrangement.get

        desSubmissionRequest.ttpArrangement.firstPaymentAmount shouldBe "1248.95"
        desSubmissionRequest.ttpArrangement.enforcementAction shouldBe "Distraint"
        desSubmissionRequest.ttpArrangement.regularPaymentAmount shouldBe "1248.95"
        desSubmissionRequest.letterAndControl.clmPymtString shouldBe s"Initial payment of ${arrangement.schedule.initialPayment} then ${arrangement.schedule.instalments.size - 1} payments of ${arrangement.schedule.instalments.head.amount} and final payment of " +
          s"${arrangement.schedule.instalments.last.amount}"
      }

    }

    "return Future[Failed]" in {
      object TestArrangementService extends TTPArrangementService {
        override val arrangementDesApiConnector = mock[ArrangementDesApiConnector]
        override val desTTPArrangementService = DesTTPArrangementService
        override val letterAndControlService = LetterAndControlService
        override val ttpArrangementRepository: TTPArrangementRepository = mock[TTPArrangementRepository]
      }

      val arrangement: TTPArrangement = ttparrangementRequest.as[TTPArrangement]

      when(TestArrangementService.arrangementDesApiConnector.submitArrangement(any(), any())(any())).thenReturn(Future.successful(false))
      val headerCarrier = new HeaderCarrier
      val response = TestArrangementService.submit(arrangement)(headerCarrier)
      ScalaFutures.whenReady(response.failed) { e =>
        e shouldBe a [RuntimeException]
        e.getMessage shouldBe "Unable to submit arrangement"
      }

    }
  }



}
