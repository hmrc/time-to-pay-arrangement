package uk.gov.hmrc.timetopay.arrangement.services

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.timetopay.arrangement.{SubmissionError, SubmissionSuccess}
import uk.gov.hmrc.timetopay.arrangement._
import uk.gov.hmrc.timetopay.arrangement.modelFormat._
import uk.gov.hmrc.timetopay.arrangement.resources._

import scala.concurrent.{Future}

class TTPArrangementServiceSpec extends UnitSpec with MockFactory with WithFakeApplication with ScalaFutures {

  val arrangement: TTPArrangement = ttparrangementRequest.as[TTPArrangement]
  val savedArrangement = ttparrangementResponse.as[TTPArrangement]
  val letterAndControlFunction = mockFunction[TTPArrangement, Future[LetterAndControl]]
  val desArrangementFunction = mockFunction[TTPArrangement, Future[DesTTPArrangement]]
  val saveArrangement = mockFunction[TTPArrangement, Future[Option[TTPArrangement]]]
  val getArrangement = mockFunction[String, Future[Option[TTPArrangement]]]
  val desSubmissionApi = mockFunction[Taxpayer, DesSubmissionRequest, Future[Either[SubmissionError, SubmissionSuccess]]]

  val arrangementService = new TTPArrangementService(desSubmissionApi, desArrangementFunction,
    letterAndControlFunction,
    saveArrangement,
    getArrangement
  )
  private val ttpArrangement: DesTTPArrangement = savedArrangement.desArrangement.get.ttpArrangement
  private val letter: LetterAndControl = savedArrangement.desArrangement.get.letterAndControl
  val request = DesSubmissionRequest(ttpArrangement, letter)

  "TTPArrangementService" should {
    "submit arrangement to DES and save the response/request combined" in {
      desArrangementFunction.expects(arrangement).returning(Future.successful(ttpArrangement))

      letterAndControlFunction.expects(arrangement).returning(Future.successful(letter))

      desSubmissionApi.expects(arrangement.taxpayer, request).returning(Future.successful(Right(SubmissionSuccess(request))))

      saveArrangement expects * returning Future.successful(Some(savedArrangement))

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

    "return failed future for DES Bad request" in {
      desArrangementFunction.expects(arrangement).returning(Future.successful(ttpArrangement))
      letterAndControlFunction.expects(arrangement).returning(Future.successful(letter))

      desSubmissionApi.expects(arrangement.taxpayer, request).returning(Future.successful(Left(SubmissionError(400, "Bad JSON"))))

      val headerCarrier = new HeaderCarrier
      val response = arrangementService.submit(arrangement)(headerCarrier)

      ScalaFutures.whenReady(response.failed) { e =>
        e shouldBe a [DesApiException]
        e.getMessage shouldBe "DES httpCode: 400, reason: Bad JSON"
      }
    }

    "return failed future for internal exception" in {
      letterAndControlFunction.expects(arrangement).returning(Future.failed(new RuntimeException("Failed to create letter and control")))

      val headerCarrier = new HeaderCarrier
      val response = arrangementService.submit(arrangement)(headerCarrier)

      ScalaFutures.whenReady(response.failed) { e =>
        e shouldBe a [RuntimeException]
        e.getMessage shouldBe "Failed to create letter and control"
      }
    }
  }

}
