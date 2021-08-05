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

package uk.gov.hmrc.timetopay.arrangement.support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping

object WireMockResponses {

  def desArrangementApiSucccess(utr: String): StubMapping = {
    stubFor(
      post(
        urlEqualTo(s"/time-to-pay/taxpayers/${utr}/arrangements")).willReturn(
          aResponse()
            .withStatus(202)
            .withBody("{}")
        ))
  }

  def desArrangementApiBadRequestClientError(utr: String): StubMapping = {
    stubFor(
      post(
        urlEqualTo(s"/time-to-pay/taxpayers/${utr}/arrangements")).willReturn(
          aResponse()
            .withStatus(400)
            .withBody("Bad JSON")))
  }

  def desArrangementApiBadRequestSeverError(utr: String): StubMapping = {
    stubFor(
      post(
        urlEqualTo(s"/time-to-pay/taxpayers/${utr}/arrangements")).willReturn(
          aResponse()
            .withStatus(500)
            .withBody(
              """{
            "code": "SERVICE_UNAVAILABLE",
            "reason": "Dependent systems are currently not responding."
}""".stripMargin)))
  }

}
