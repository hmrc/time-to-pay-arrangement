/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.http.Status
import uk.gov.hmrc.http._
import uk.gov.hmrc.timetopay.arrangement.DesSubmissionRequest
import uk.gov.hmrc.timetopay.arrangement.config.DesArrangementApiServiceConnectorConfig
import uk.gov.hmrc.timetopay.arrangement.resources.{submitArrangementLetterAndControl, submitArrangementTTPArrangement, taxpayer}
import uk.gov.hmrc.timetopay.arrangement.support.{ITSpec, WireMockResponses}

class DesArrangementApiServiceConnectorSpec extends ITSpec {

  val desArrangementApiServiceConnectorConfig = fakeApplication().injector.instanceOf[DesArrangementApiServiceConnectorConfig]
  val connector = fakeApplication().injector.instanceOf[DesArrangementApiServiceConnector]

  val request: DesSubmissionRequest = DesSubmissionRequest(submitArrangementTTPArrangement, submitArrangementLetterAndControl)
  "Calling submitArrangement should return 202 accepted response" in {

    WireMockResponses.desArrangementApiSucccess(taxpayer.selfAssessment.utr)

    val result = connector.submitArrangement(taxpayer, request).futureValue

    result.right.value shouldBe SubmissionSuccess()

  }
  "Calling submitArrangement should return 400 response" in {

    WireMockResponses.desArrangementApiBadRequest(taxpayer.selfAssessment.utr)

    val result = connector.submitArrangement(taxpayer, request).futureValue

    result.left.value.code shouldBe 400
  }

}
