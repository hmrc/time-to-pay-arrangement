/*
 * Copyright 2019 HM Revenue & Customs
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

import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.timetopay.arrangement.config.DesArrangementApiService
import uk.gov.hmrc.timetopay.arrangement.modelFormat._
import uk.gov.hmrc.timetopay.arrangement.resources._
import uk.gov.hmrc.timetopay.arrangement._
import org.mockito.{ArgumentMatchers => Args}
import org.scalatest.concurrent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

class TTPArrangementServiceSpec extends PlaySpec  with OneAppPerSuite with MockitoSugar {
  implicit val defaultPatience: concurrent.ScalaFutures.PatienceConfig = ScalaFutures.PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))
  val mockDesAPi: DesArrangementService = mock[DesArrangementService]
  val mockTTpRepo: TTPArrangementRepository = mock[TTPArrangementRepository]

  val arrangement: TTPArrangement = ttparrangementRequest.as[TTPArrangement]
  val savedArrangement: TTPArrangement = ttparrangementResponse.as[TTPArrangement]

  val letterAndControlMock: LetterAndControlBuilder = mock[LetterAndControlBuilder]
  val desTTPArrangementBuilderMock: DesTTPArrangementBuilder = mock[DesTTPArrangementBuilder]
  val ttpArrangementRepository: TTPArrangementRepository = mock[TTPArrangementRepository]
  val desSubmissionApiMock: DesArrangementApiService = mock[DesArrangementApiService]

  lazy val arrangementService = new TTPArrangementService(desTTPArrangementBuilderMock,desSubmissionApiMock,ttpArrangementRepository,letterAndControlMock)

  private val ttpArrangement: DesTTPArrangement = savedArrangement.desArrangement.get.ttpArrangement
  private val letter: LetterAndControl = savedArrangement.desArrangement.get.letterAndControl
  val request = DesSubmissionRequest(ttpArrangement, letter)

  "TTPArrangementService" should {
    "submit arrangement to DES and save the response/request combined" in {
      when(letterAndControlMock.create(arrangement)).thenReturn(letter)
      when(desTTPArrangementBuilderMock.create(arrangement)).thenReturn(ttpArrangement)
      when(desSubmissionApiMock.submitArrangement(arrangement.taxpayer,request)).thenReturn(Future.successful(Right(SubmissionSuccess())))
      when(ttpArrangementRepository.save(Args.any[TTPArrangement])).thenReturn(Future.successful(Some(savedArrangement)))

      val response = arrangementService.submit(arrangement)(HeaderCarrier())

      ScalaFutures.whenReady(response) { r =>
        val desSubmissionRequest = r.get.desArrangement.get

        desSubmissionRequest.ttpArrangement.firstPaymentAmount mustBe "1248.95"
        desSubmissionRequest.ttpArrangement.enforcementAction mustBe "Distraint"
        desSubmissionRequest.ttpArrangement.regularPaymentAmount mustBe "1248.95"
        desSubmissionRequest.letterAndControl.clmPymtString mustBe s"Initial payment of " +
          s"${arrangement.schedule.initialPayment} then ${arrangement.schedule.instalments.size - 1} " +
          s"payments of ${arrangement.schedule.instalments.head.amount} and final payment of " +
          s"${arrangement.schedule.instalments.last.amount}"
      }
    }

    "return failed future for DES Bad request" in {

      when(letterAndControlMock.create(arrangement)).thenReturn(letter)
      when(desTTPArrangementBuilderMock.create(arrangement)).thenReturn(ttpArrangement)
      when(desSubmissionApiMock.submitArrangement(arrangement.taxpayer,request)).thenReturn(Future.successful(Left(SubmissionError(400, "Bad JSON"))))
      when(ttpArrangementRepository.save(Args.any[TTPArrangement])).thenReturn(Future.successful(Some(savedArrangement)))

      val headerCarrier = new HeaderCarrier
      val response = arrangementService.submit(arrangement)(headerCarrier)

      ScalaFutures.whenReady(response.failed) { e =>
        e mustBe a[DesApiException]
        e.getMessage mustBe "DES httpCode: 400, reason: Bad JSON"
      }
    }

    "return No arrangement if saving fails" in {
      when(letterAndControlMock.create(arrangement)).thenReturn(letter)
      when(desTTPArrangementBuilderMock.create(arrangement)).thenReturn(ttpArrangement)
      when(desSubmissionApiMock.submitArrangement(arrangement.taxpayer,request)).thenReturn(Future.successful(Right(SubmissionSuccess())))
      when(ttpArrangementRepository.save(Args.any[TTPArrangement])).thenReturn(Future.successful(None))

      val headerCarrier = new HeaderCarrier
      val response = arrangementService.submit(arrangement)(headerCarrier)

      ScalaFutures.whenReady(response) {
        r => r mustBe None
      }
    }

    "return failed future for internal exception" in {
      when(letterAndControlMock.create(arrangement)).thenThrow(new RuntimeException("Failed to create letter and control"))
      val exception =
        intercept[RuntimeException] {
          val headerCarrier = new HeaderCarrier
          arrangementService.submit(arrangement)(headerCarrier)
        }

      exception.getMessage mustBe "Failed to create letter and control"
    }
  }
}
