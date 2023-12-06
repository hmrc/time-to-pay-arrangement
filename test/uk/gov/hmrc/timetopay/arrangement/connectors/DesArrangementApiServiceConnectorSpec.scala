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

package uk.gov.hmrc.timetopay.arrangement.connectors

import uk.gov.hmrc.timetopay.arrangement.model.DesSubmissionRequest
import uk.gov.hmrc.timetopay.arrangement.support.{ITSpec, TestData, WireMockResponses}

class DesArrangementApiServiceConnectorSpec extends ITSpec with TestData {

  private val connector = app.injector.instanceOf[DesArrangementApiServiceConnector]

  private val request: DesSubmissionRequest = DesSubmissionRequest(submitArrangementTTPArrangement, submitArrangementLetterAndControl)

  "Calling submitArrangement should return 202 accepted response" in {

    WireMockResponses.desArrangementApiSucccess(taxpayer.selfAssessment.utr)

    val result = connector.submitArrangement(taxpayer.selfAssessment.utr, request).futureValue

    result shouldBe SubmissionSuccess()

  }
  "Calling submitArrangement should return 400 response" in {

    WireMockResponses.desArrangementApiBadRequestClientError(taxpayer.selfAssessment.utr)

    val result = connector.submitArrangement(taxpayer.selfAssessment.utr, request).futureValue

    result match {
      case SubmissionError(code, _) => code shouldBe 400
      case err                      => fail(err.toString)
    }
  }

}
