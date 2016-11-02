package uk.gov.hmrc.timetopay.arrangement.connectors

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.timetopay.arrangement.models.{DesTTPArrangement, LetterAndControl}
import uk.gov.hmrc.timetopay.arrangement.resources._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ArrangementDesApiConnectorSpec extends UnitSpec with MockitoSugar with ServicesConfig with WithFakeApplication {
  implicit val headerCarrier = HeaderCarrier()

  object TestArrangementDesApiConnector extends ArrangementDesApiConnector {
    override val desArrangementUrl: String = "des-arrangement-api-url"
    override val http: HttpGet with HttpPost = mock[WSHttp]
  }

  "Calling submitArrangement" should {
    "return 202 accepted response" in {
      val response = HttpResponse(Status.ACCEPTED)
      when(TestArrangementDesApiConnector.http.POST[JsValue, HttpResponse](any[String], any[JsValue], any())(any(), any(), any()))
        .thenReturn(Future(response))

      val result = TestArrangementDesApiConnector.submitArrangement(taxpayer, submitArrangementTTPArrangement, submitArrangementLetterAndControl)

      ScalaFutures.whenReady(result) { r =>
        r shouldBe a[HttpResponse]
        r.status shouldBe Status.ACCEPTED
      }
    }
  }
}
