package uk.gov.hmrc.timetopay.arrangement

import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.timetopay.arrangement.controllers.MicroserviceHelloWorld


class MicroserviceHelloWorldControllerSpec extends UnitSpec with WithFakeApplication{

  val fakeRequest = FakeRequest("GET", "/")


  "GET /" should {
    "return 200" in {
      val result = MicroserviceHelloWorld.hello()(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }


}
