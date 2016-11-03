package uk.gov.hmrc.timetopay.arrangement.connectors

import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.timetopay.arrangement.WSHttp
import uk.gov.hmrc.timetopay.arrangement.models.{DesSubmissionRequest, Taxpayer}
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object ArrangementDesApiConnector extends ArrangementDesApiConnector {
  override val desArrangementUrl = baseUrl("des-arrangement-api")
  override val http: HttpGet with HttpPost = WSHttp
}

trait ArrangementDesApiConnector extends ServicesConfig {
  val desArrangementUrl: String
  val http: HttpGet with HttpPost

  def submitArrangement(taxpayer: Taxpayer, desSubmissionRequest: DesSubmissionRequest)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val serviceUrl = s"time-to-pay/taxpayers/${taxpayer.selfAssessment.utr}/arrangements"
    http.POST[JsValue, HttpResponse](s"$desArrangementUrl/$serviceUrl", Json.toJson(desSubmissionRequest))
      .map (_.status == Status.ACCEPTED)
  }
}

