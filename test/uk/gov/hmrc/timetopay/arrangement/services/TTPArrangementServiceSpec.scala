/*
 * Copyright 2023 HM Revenue & Customs
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

import uk.gov.hmrc.mongo.workitem.WorkItem
import uk.gov.hmrc.timetopay.arrangement.model.{TTPArrangement, TTPArrangementWorkItem}
import uk.gov.hmrc.timetopay.arrangement.repository.TTPArrangementWorkItemRepository
import uk.gov.hmrc.timetopay.arrangement.support.{ITSpec, TestData, WireMockResponses}

class TTPArrangementServiceSpec extends ITSpec with TestData {

  import Taxpayers._

  private val arrangementWorkItemRepo = fakeApplication().injector.instanceOf[TTPArrangementWorkItemRepository]
  private val tTPArrangementService = fakeApplication().injector.instanceOf[TTPArrangementService]
  private val arrangement: TTPArrangement = ttparrangementRequest.as[TTPArrangement].copy(taxpayer = taxPayerWithEnglishAddress)

  override def beforeEach(): Unit = {
    arrangementWorkItemRepo.collection.drop().toFuture().futureValue
    ()
  }

  override def afterEach(): Unit = {
    arrangementWorkItemRepo.collection.drop().toFuture().futureValue
    ()
  }

  "TTPArrangementService should submit arrangement to DES and save the response/request combined" in {

    WireMockResponses.desArrangementApiSucccess(arrangement.taxpayer.selfAssessment.utr)

    val response = tTPArrangementService.submit(arrangement).futureValue

    val desSubmissionRequest = response.desArrangement.get

    logger.warn(desSubmissionRequest.toString)

    desSubmissionRequest.ttpArrangement.firstPaymentAmount shouldBe "1248.95"
    desSubmissionRequest.ttpArrangement.enforcementAction shouldBe "Distraint"
    desSubmissionRequest.ttpArrangement.regularPaymentAmount shouldBe "1248.95"
  }

  "TTPArrangementService should return failed future for DES Bad request in the 500's range and save to the work item db in" in {

    WireMockResponses.desArrangementApiBadRequestServerError(arrangement.taxpayer.selfAssessment.utr)
    val response = tTPArrangementService.submit(arrangement).failed.futureValue
    val workItem: Option[WorkItem[TTPArrangementWorkItem]] = arrangementWorkItemRepo.collection.find().headOption().futureValue
    workItem should not be None
    response.getMessage should include("SERVICE_UNAVAILABLE")
  }

  "TTPArrangementService should return failed future for nginx timeout (499) and save to the work item db in" in {

    WireMockResponses.desArrangementApiBadRequestServerError(arrangement.taxpayer.selfAssessment.utr)
    val response = tTPArrangementService.submit(arrangement).failed.futureValue
    val workItem: Option[WorkItem[TTPArrangementWorkItem]] = arrangementWorkItemRepo.collection.find().headOption().futureValue
    workItem should not be None
    response.getMessage should include("SERVICE_UNAVAILABLE")
  }

  "TTPArrangementService should return failed future for DES Bad request in the 400's range and not save to the work item db in" in {

    WireMockResponses.desArrangementApiBadRequestClientError(arrangement.taxpayer.selfAssessment.utr)
    val response = tTPArrangementService.submit(arrangement).failed.futureValue
    val workItem: Option[WorkItem[TTPArrangementWorkItem]] = arrangementWorkItemRepo.collection.find().headOption().futureValue
    workItem shouldBe None
    response.getMessage should include("DES httpCode: 400")
  }

}
