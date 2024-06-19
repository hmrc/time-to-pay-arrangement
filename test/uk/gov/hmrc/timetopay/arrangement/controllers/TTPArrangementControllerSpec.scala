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

package uk.gov.hmrc.timetopay.arrangement.controllers

import play.api.http.Status
import play.api.libs.json._
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HttpResponse, StringContextOps}
import uk.gov.hmrc.timetopay.arrangement.support.{ITSpec, TestData, WireMockResponses}

class TTPArrangementControllerSpec extends ITSpec with TestData {

  "POST /ttparrangements should" - {

    "return 201" in {
      WireMockResponses.authorise()
      WireMockResponses.desArrangementApiSucccess("1234567890")

      val result = await(httpClient.post(url"$baseUrl/ttparrangements")
        .withBody(ttparrangementRequest)
        .execute[HttpResponse])
      result.status shouldBe Status.CREATED

      WireMockResponses.ensureAuthoriseCalled()
    }

    "return 201 when no postcode is provided (bug fix OPS-5756)" in {
      WireMockResponses.authorise()
      WireMockResponses.desArrangementApiSucccess("1234567890")

      val requestWithNoPostcode: JsValue = ttparrangementRequest.transform(
        (__ \ "taxpayer" \ "addresses").json.update {
          __.read[JsArray].map {
            case JsArray(addressObjs) => JsArray(addressObjs.map(_.as[JsObject] - "postcode"))
          }
        }
      ).get

      val result: HttpResponse = await(httpClient.post(url"$baseUrl/ttparrangements")
        .withBody(requestWithNoPostcode)
        .execute[HttpResponse])

      result.status shouldBe Status.CREATED

      WireMockResponses.ensureAuthoriseCalled()
    }

    "return 500 if arrangement service fails" in {
      WireMockResponses.authorise()
      WireMockResponses.desArrangementApiBadRequestClientError("1234567890")

      val result = await(httpClient.post(url"$baseUrl/ttparrangements")
        .withBody(ttparrangementRequest)
        .execute[HttpResponse])

      result.status shouldBe 500
      result.body should include("Submission to DES failed, status code [400]")
      result.body should include("Queued for retry: false")

      WireMockResponses.ensureAuthoriseCalled()
    }

    "return 500 if DES service unavailable" in {
      WireMockResponses.authorise()
      WireMockResponses.desArrangementApiBadRequestServerError("1234567890")

      //      val result = await(httpClient.POST[JsValue, HttpResponse](s"$baseUrl/ttparrangements", ttparrangementRequest))

      val result = await(httpClient.post(url"$baseUrl/ttparrangements")
        .withBody(ttparrangementRequest)
        .execute[HttpResponse])

      result.status shouldBe 500
      result.body should include(
        """Submission to DES failed, status code [500] and response [{
                |            "code": "SERVICE_UNAVAILABLE",
                |            "reason": "Dependent systems are currently not responding."
                |}]. Queued for retry: true""".stripMargin
      )

      WireMockResponses.ensureAuthoriseCalled()
    }

  }

}
