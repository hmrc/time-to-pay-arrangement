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

import play.api.Logger
import uk.gov.hmrc.timetopay.arrangement.config.{DesArrangementApiServiceConnectorConfig, LetterAndControlAndJurisdictionChecker}
import uk.gov.hmrc.timetopay.arrangement.connectors.DesArrangementApiServiceConnector
import uk.gov.hmrc.timetopay.arrangement.modelFormat._
import uk.gov.hmrc.timetopay.arrangement.resources._
import uk.gov.hmrc.timetopay.arrangement.support.{ITSpec, WireMockResponses}
import uk.gov.hmrc.timetopay.arrangement._
import uk.gov.hmrc.timetopay.arrangement.repository.TTPArrangementRepository

import scala.concurrent.ExecutionContext.Implicits.global


//DesArrangementApiService = DesArrangementApiServiceConnectorConfig

//DesArrangementService = DesArrangementApiServiceConnector

class TTPArrangementServiceSpec extends ITSpec {

  val arrangementRepo = fakeApplication.injector.instanceOf[TTPArrangementRepository]

  override def beforeEach() {
    val temp = arrangementRepo.collection.drop(false).futureValue
  }

  override def afterEach() {
    val temp = arrangementRepo.collection.drop(false).futureValue
  }


  val desArrangementApiServiceConnectorConfig = fakeApplication().injector.instanceOf[DesArrangementApiServiceConnectorConfig]
  val desArrangementApiServiceConnector = fakeApplication().injector.instanceOf[DesArrangementApiServiceConnector]
  val letterAndControlAndJurisdictionChecker = fakeApplication().injector.instanceOf[LetterAndControlAndJurisdictionChecker]
  val desTTPArrangementBuilder = fakeApplication().injector.instanceOf[DesTTPArrangementBuilder]
  val tTPArrangementRepository = fakeApplication().injector.instanceOf[TTPArrangementRepository]
  val letterAndControlBuilder = fakeApplication().injector.instanceOf[LetterAndControlBuilder]
  val tTPArrangementService = fakeApplication().injector.instanceOf[TTPArrangementService]


  val arrangement: TTPArrangement = ttparrangementRequest.as[TTPArrangement]
  val savedArrangement: TTPArrangement = ttparrangementResponse.as[TTPArrangement]

  private val ttpArrangement: DesTTPArrangement = savedArrangement.desArrangement.get.ttpArrangement
  private val letter: LetterAndControl = savedArrangement.desArrangement.get.letterAndControl
  val request = DesSubmissionRequest(ttpArrangement, letter)


  "TTPArrangementService should submit arrangement to DES and save the response/request combined" in {

    WireMockResponses.desArrangementApiSucccess(arrangement.taxpayer.selfAssessment.utr)

    val response = tTPArrangementService.submit(arrangement).futureValue

    val desSubmissionRequest = response.get.desArrangement.get

    Logger.warn(desSubmissionRequest.toString)

    desSubmissionRequest.ttpArrangement.firstPaymentAmount shouldBe "1298.95"
    desSubmissionRequest.ttpArrangement.enforcementAction shouldBe "Distraint"
    desSubmissionRequest.ttpArrangement.regularPaymentAmount shouldBe "1248.95"
    desSubmissionRequest.letterAndControl.clmPymtString shouldBe "Initial payment of £1,298.95 then 2 payments of £1,248.95 and final payment of £1,248.95"

  }

      "TTPArrangementService should return failed future for DES Bad request" in {

        WireMockResponses.desArrangementApiBadRequest(arrangement.taxpayer.selfAssessment.utr)
        val response = tTPArrangementService.submit(arrangement).failed.futureValue
        response.getMessage should include("DES httpCode: 400")
      }
}
