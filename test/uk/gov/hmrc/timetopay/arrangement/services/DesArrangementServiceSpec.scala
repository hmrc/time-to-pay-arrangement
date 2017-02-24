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

import javax.inject.Inject

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.libs.json.Writes
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.timetopay.arrangement.DesSubmissionRequest
import uk.gov.hmrc.timetopay.arrangement.resources._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DesArrangementServiceSpec extends UnitSpec  with ScalaFutures with MockFactory  {
  implicit val headerCarrier = HeaderCarrier()

  val mockHttp: HttpGet with HttpPost = mock[WSHttp]

  class TestDesArrangementApiService extends DesArrangementService {
    override val desArrangementUrl: String = "des-arrangement-api-url"
    override val http: HttpGet with HttpPost = mockHttp
    override val authorisationToken = "token"
    override val serviceEnvironment = "env"
  }

  val connector = new TestDesArrangementApiService

  "Calling submitArrangement" should {
    val request: DesSubmissionRequest = DesSubmissionRequest(submitArrangementTTPArrangement, submitArrangementLetterAndControl)
    "return 202 accepted response" in {

      (mockHttp.POST(_:String, _:DesSubmissionRequest, _:Seq[(String,String)])(_:Writes[DesSubmissionRequest], _:HttpReads[HttpResponse], _:HeaderCarrier))
        .expects(*,*,*,*,*,*).returning(Future.successful(HttpResponse(Status.ACCEPTED)))

      val result = connector.submitArrangement(taxpayer,  request).futureValue

      result.right.get shouldBe SubmissionSuccess()

    }
    "return 400 response" in {

      val response = HttpResponse(responseStatus = Status.BAD_REQUEST, responseString= Some("JSON Not valid"))

      (mockHttp.POST(_:String, _:DesSubmissionRequest, _:Seq[(String,String)])(_:Writes[DesSubmissionRequest], _:HttpReads[HttpResponse], _:HeaderCarrier))
        .expects(*,*,*,*,*,*).returning(Future.failed(Upstream4xxResponse("JSON Not valid", 400, 500, Map())))

      val result = connector.submitArrangement(taxpayer, request).futureValue

      result.left.get.message shouldBe "JSON Not valid"
    }
  }
}
