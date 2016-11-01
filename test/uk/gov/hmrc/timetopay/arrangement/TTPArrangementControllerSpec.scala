package uk.gov.hmrc.timetopay.arrangement

import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.timetopay.arrangement.controllers.TTPArrangementController

import scala.io.Source

class TTPArrangementControllerSpec extends UnitSpec with WithFakeApplication {


  "POST /ttparrangements" should {
    "return 201" in {
      val source: String = Source.fromInputStream(getClass.getResourceAsStream("/test.json")).getLines.mkString
      val json: JsValue = Json.parse(source)

      val fakeRequest = FakeRequest("POST", "/ttparrangements").withBody(json)
      val result = TTPArrangementController.create().apply(fakeRequest)
      status(result) shouldBe Status.CREATED
    }
  }
}
