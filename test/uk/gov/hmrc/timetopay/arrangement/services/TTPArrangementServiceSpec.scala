/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.timetopay.arrangement.services

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.timetopay.arrangement._
import uk.gov.hmrc.timetopay.arrangement.modelFormat._
import uk.gov.hmrc.timetopay.arrangement.resources._

import scala.concurrent.Future

class TTPArrangementServiceSpec extends UnitSpec with MockFactory  with ScalaFutures with OneAppPerSuite{

  val arrangement: TTPArrangement = ttparrangementRequest.as[TTPArrangement]
  val savedArrangement = ttparrangementResponse.as[TTPArrangement]
  val letterAndControlFunction = mockFunction[TTPArrangement, LetterAndControl]
  val desArrangementFunction = mockFunction[TTPArrangement, DesTTPArrangement]
  val saveArrangement = mockFunction[TTPArrangement, Future[Option[TTPArrangement]]]
  val getArrangement = mockFunction[String, Future[Option[TTPArrangement]]]
  val desSubmissionApi = mockFunction[Taxpayer, DesSubmissionRequest, Future[Either[SubmissionError, SubmissionSuccess]]]

  val arrangementService = new TTPArrangementService()
  private val ttpArrangement: DesTTPArrangement = savedArrangement.desArrangement.get.ttpArrangement
  private val letter: LetterAndControl = savedArrangement.desArrangement.get.letterAndControl
  val request = DesSubmissionRequest(ttpArrangement, letter)

  "TTPArrangementService" should {
    "submit arrangement to DES and save the response/request combined" in {
      desArrangementFunction.expects(arrangement).returning(ttpArrangement)

      letterAndControlFunction.expects(arrangement).returning(letter)

      desSubmissionApi.expects(arrangement.taxpayer, request).returning(Future.successful(Right(SubmissionSuccess())))

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
      desArrangementFunction.expects(arrangement).returning(ttpArrangement)
      letterAndControlFunction.expects(arrangement).returning(letter)

      desSubmissionApi.expects(arrangement.taxpayer, request).returning(Future.successful(Left(SubmissionError(400, "Bad JSON"))))

      saveArrangement expects * returning Future.successful(Some(savedArrangement))

      val headerCarrier = new HeaderCarrier
      val response = arrangementService.submit(arrangement)(headerCarrier)

      ScalaFutures.whenReady(response.failed) { e =>
        e shouldBe a [DesApiException]
        e.getMessage shouldBe "DES httpCode: 400, reason: Bad JSON"
      }
    }

    "return No arrangement if saving fails" in {
      desArrangementFunction.expects(arrangement).returning(ttpArrangement)
      letterAndControlFunction.expects(arrangement).returning(letter)

      desSubmissionApi.expects(arrangement.taxpayer, request).returning(Future.successful(Right(SubmissionSuccess())))

      saveArrangement expects * returning Future.successful(None)

      val headerCarrier = new HeaderCarrier
      val response = arrangementService.submit(arrangement)(headerCarrier)

      ScalaFutures.whenReady(response) {
        r => r shouldBe None
      }
    }

    "return No arrangement if DB connection fails" in {
      desArrangementFunction.expects(arrangement).returning(ttpArrangement)
      letterAndControlFunction.expects(arrangement).returning(letter)

      desSubmissionApi.expects(arrangement.taxpayer, request).returning(Future.successful(Right(SubmissionSuccess())))

      saveArrangement expects * throwing new RuntimeException("Couldn't connect to mongo")

      val headerCarrier = new HeaderCarrier
      val response = arrangementService.submit(arrangement)(headerCarrier)

      ScalaFutures.whenReady(response) {
        r => r shouldBe None
      }
    }



    "return failed future for internal exception" in {
      letterAndControlFunction.expects(arrangement).throwing(new RuntimeException("Failed to create letter and control"))
      val exception =
        intercept[RuntimeException] {
          val headerCarrier = new HeaderCarrier
          arrangementService.submit(arrangement)(headerCarrier)
        }

      exception.getMessage shouldBe "Failed to create letter and control"
    }
  }

}
