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

package uk.gov.hmrc.timetopay.arrangement.controllers

import play.api.http.Status
import play.api.libs.json._
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.timetopay.arrangement.repository.TTPArrangementRepository
import uk.gov.hmrc.timetopay.arrangement.resources._
import uk.gov.hmrc.timetopay.arrangement.support.{ITSpec, WireMockResponses}

class TTPArrangementControllerSpec extends ITSpec {

  private val arrangementRepo = fakeApplication.injector.instanceOf[TTPArrangementRepository]

  override def beforeEach(): Unit = {
    arrangementRepo.collection.drop(false).futureValue
    ()
  }

  override def afterEach(): Unit = {
    arrangementRepo.collection.drop(false).futureValue
    ()
  }

  "POST /ttparrangements should return 201" in {

    WireMockResponses.desArrangementApiSucccess("1234567890")
    val result: HttpResponse = httpClient.POST[JsValue, HttpResponse](s"$baseUrl/ttparrangements", ttparrangementRequest).futureValue
    result.status shouldBe Status.CREATED
    result.header("Location") should not be None
  }

  "POST /ttparrangements should return 201 when no postcode is provided (bug fix OPS-5756)" in {

    WireMockResponses.desArrangementApiSucccess("1234567890")

    val requestWithNoPostcode: JsValue = ttparrangementRequest.transform(
      (__ \ "taxpayer" \ "addresses").json.update {
        __.read[JsArray].map {
          case JsArray(addressObjs) => JsArray(addressObjs.map(_.as[JsObject] - "postcode"))
        }
      }
    ).get

    val result: HttpResponse = httpClient.POST[JsValue, HttpResponse](s"$baseUrl/ttparrangements", requestWithNoPostcode).futureValue
    result.status shouldBe Status.CREATED
    result.header("Location") should not be None
  }

  "POST /ttparrangements should return 500 if arrangement service fails" in {

    WireMockResponses.desArrangementApiBadRequest("1234567890")
    val result = httpClient.POST[JsValue, HttpResponse](s"$baseUrl/ttparrangements", ttparrangementRequest)
      .futureValue

    result.status shouldBe 500
    result.body should include("Submission to DES failed, status code [400]")

  }

  "GET /ttparrangements should return 200 for arrangement" in {

    WireMockResponses.desArrangementApiSucccess("1234567890")
    val result: HttpResponse = httpClient.POST[JsValue, HttpResponse](s"$baseUrl/ttparrangements", ttparrangementRequest).futureValue
    result.status shouldBe Status.CREATED
    val nextUrl = result.header("Location").get

    val result2 = httpClient.GET[HttpResponse](nextUrl).futureValue
    result2.status shouldBe Status.OK
  }

  "GET /ttparrangements should return 404 for non existent arrangement" in {
    val result = httpClient
      .GET[HttpResponse]("http://localhost:19001/ttparrangements/22f9d802-3a34-45a9-bbb4-f5d6433bf3ff")
      .futureValue

    result.status shouldBe 404
    result.body should include("does not exist")

  }

}
