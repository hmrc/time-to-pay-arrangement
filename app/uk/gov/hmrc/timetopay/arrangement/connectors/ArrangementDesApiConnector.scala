package uk.gov.hmrc.timetopay.arrangement.connectors

import play.api.libs.json.{Json, JsValue}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse, HttpGet, HttpPost}
import uk.gov.hmrc.timetopay.arrangement.WSHttp
import uk.gov.hmrc.timetopay.arrangement.models.{Taxpayer, LetterAndControl, DesTTPArrangement}

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._
import scala.concurrent.Future


object ArrangementDesApiConnector extends ArrangementDesApiConnector {
  override val desArrangementUrl = baseUrl("des-arrangement-api")
  override val http: HttpGet with HttpPost = WSHttp
}

trait ArrangementDesApiConnector extends ServicesConfig {
  val desArrangementUrl: String
  val http: HttpGet with HttpPost

  def submitArrangement(taxpayer: Taxpayer, desTTPArrangement: DesTTPArrangement,
                        letterAndControl: LetterAndControl)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val serviceUrl = s"time-to-pay/taxpayers/${taxpayer.selfAssessment.utr}/arrangements"
    http.POST[JsValue, HttpResponse](s"$desArrangementUrl/$serviceUrl", Json.toJson(DesSubmissionRequest(desTTPArrangement, letterAndControl)))
  }
}

case class DesSubmissionRequest(ttpArrangement: DesTTPArrangement, letterAndControl: LetterAndControl) {}
