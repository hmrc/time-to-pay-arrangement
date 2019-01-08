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

package uk.gov.hmrc.timetopay.arrangement.controllers

import akka.stream.Materializer
import javax.inject.Inject
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.timetopay.arrangement.modelFormat._
import uk.gov.hmrc.timetopay.arrangement.resources._
import uk.gov.hmrc.timetopay.arrangement.services._
import uk.gov.hmrc.timetopay.arrangement.{TTPArrangement, TTPArrangementController}

import scala.concurrent.Future
class TTPArrangementControllerSpec extends UnitSpec with MockFactory with ScalaFutures {

  class MockService extends TTPArrangementService(null,null,null,null) {}
  val arrangementServiceStub = stub[MockService]
  implicit val ec =  scala.concurrent.ExecutionContext.Implicits.global
  val arrangementController = new TTPArrangementController(arrangementServiceStub)

  "POST /ttparrangements" should {
    "return 201" in {
      implicit val hc = HeaderCarrier
      (arrangementServiceStub.submit(_: TTPArrangement)(_: HeaderCarrier)).when(ttparrangementRequest.as[TTPArrangement], *) returns Some(ttparrangementResponse.as[TTPArrangement])

      val fakeRequest = FakeRequest("POST", "/ttparrangements").withBody(ttparrangementRequest)
      val result = arrangementController.create().apply(fakeRequest).futureValue
      status(result) shouldBe Status.CREATED
      result.header.headers.get("Location") should not be None
    }

    "return 201 without header if saving arrangement fails" in {
      implicit val hc = HeaderCarrier
      (arrangementServiceStub.submit(_: TTPArrangement)(_: HeaderCarrier)).when(ttparrangementRequest.as[TTPArrangement], *) returns None

      val fakeRequest = FakeRequest("POST", "/ttparrangements").withBody(ttparrangementRequest)
      val result = arrangementController.create()(fakeRequest).futureValue
      status(result) shouldBe Status.CREATED
      result.header.headers.get("Location") shouldBe None
    }

    "return 500 if arrangement service fails" in {
      implicit val hc = HeaderCarrier
      (arrangementServiceStub.submit(_: TTPArrangement)(_: HeaderCarrier)).when(ttparrangementRequest.as[TTPArrangement], *) returns Future.failed(new RuntimeException("****Simulated exception**** Internal processing exception"))

      val fakeRequest = FakeRequest("POST", "/ttparrangements").withBody(ttparrangementRequest)
      val result: Result = arrangementController.create()(fakeRequest).futureValue
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "return 500 if arrangement service throws desapiexception" in {
      implicit val hc = HeaderCarrier
      (arrangementServiceStub.submit(_: TTPArrangement)(_: HeaderCarrier)).when(ttparrangementRequest.as[TTPArrangement], *) returns Future.failed(new DesApiException(403, "Forbidden"))

      val fakeRequest = FakeRequest("POST", "/ttparrangements").withBody(ttparrangementRequest)
      val result = arrangementController.create()(fakeRequest).futureValue
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  "GET /ttparrangements" should {
    "return 200 for arrangement" in {

      (arrangementServiceStub.byId(_: String)).when("XXX-XXX-XXX") returns Future.successful(Some(ttparrangementResponse))

      val fakeRequest = FakeRequest("GET", "/ttparrangements/XXX-XXX-XXX")
      val result = arrangementController.arrangement("XXX-XXX-XXX")(fakeRequest).futureValue
      status(result) shouldBe Status.OK
    }

    "return 404 for non existent arrangement" in {

      (arrangementServiceStub.byId(_: String)).when("XXX-XXX-XXX") returns Future.successful(None)

      val fakeRequest = FakeRequest("GET", "/ttparrangements/XXX-XXX-XXX")
      val result = arrangementController.arrangement("XXX-XXX-XXX").apply(fakeRequest).futureValue
      status(result) shouldBe Status.NOT_FOUND

    }
  }
}
