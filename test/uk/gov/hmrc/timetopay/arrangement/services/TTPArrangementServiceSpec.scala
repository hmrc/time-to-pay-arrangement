package uk.gov.hmrc.timetopay.arrangement.services

import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.{HeaderCarrier}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.timetopay.arrangement.connectors.{SubmissionError, SubmissionSuccess, ArrangementDesApiConnector}
import uk.gov.hmrc.timetopay.arrangement.models.{DesSubmissionRequest, TTPArrangement}
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._
import uk.gov.hmrc.timetopay.arrangement.resources._
import org.mockito.Matchers._
import uk.gov.hmrc.timetopay.arrangement.repositories.TTPArrangementRepository

import scala.concurrent.Future

class TTPArrangementServiceSpec extends UnitSpec with MockitoSugar with WithFakeApplication with ScalaFutures {

  val arrangementDesApiConnector = mock[ArrangementDesApiConnector]
  val desTTPArrangementService = new DesTTPArrangementService
  val letterAndControlService = new LetterAndControlService
  val ttpArrangementRepository: TTPArrangementRepository = mock[TTPArrangementRepository]


  val arrangementService = new TTPArrangementService(arrangementDesApiConnector,
    desTTPArrangementService, letterAndControlService, ttpArrangementRepository)

  "TTPArrangementService" should {
    "return Future[Success]" in {

      val arrangement: TTPArrangement = ttparrangementRequest.as[TTPArrangement]
      val savedArrangement = ttparrangementResponse.as[TTPArrangement]


      when(ttpArrangementRepository.save(any)).thenReturn(Future.successful(Some(savedArrangement)))

      val letterAndControl =  letterAndControlService.create(arrangement).futureValue
      val desArrangement = desTTPArrangementService.create(arrangement).futureValue

      when(arrangementDesApiConnector.submitArrangement(any(), any())(any()))
        .thenReturn(Future.successful(Right(SubmissionSuccess(DesSubmissionRequest(desArrangement, letterAndControl)))))


      val response = arrangementService.submit(arrangement)(new HeaderCarrier)

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

      val arrangement: TTPArrangement = ttparrangementRequest.as[TTPArrangement]

      when(arrangementDesApiConnector.submitArrangement(any(), any())(any()))
        .thenReturn(Future.successful(Left(SubmissionError("Bad JSON"))))

      val headerCarrier = new HeaderCarrier
      val response = arrangementService.submit(arrangement)(headerCarrier)
      ScalaFutures.whenReady(response.failed) { e =>
        e shouldBe a [RuntimeException]
        e.getMessage shouldBe "Bad JSON"
      }

    }
  }

}
