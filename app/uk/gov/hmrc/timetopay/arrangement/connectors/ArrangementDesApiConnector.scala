package uk.gov.hmrc.timetopay.arrangement.connectors

import play.api.{Play, Logger}
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.Authorization
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.timetopay.arrangement.WSHttp
import uk.gov.hmrc.timetopay.arrangement.models.{DesSubmissionRequest, Taxpayer}
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object ArrangementDesApiConnector extends ArrangementDesApiConnector {
  override val desArrangementUrl = baseUrl("des-arrangement-api")
  override val http: HttpGet with HttpPost = WSHttp
}

trait ArrangementDesApiConnector extends ServicesConfig {
  val desArrangementUrl: String
  val http: HttpGet with HttpPost

  val authorisationToken = getConfString("des-arrangement-api.authorization-token", "not-found")
  val serviceEnvironment = getConfString("des-arrangement-api.environment", "unknown")

  val desHeaderCarrier: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(s"Bearer $authorisationToken")),
    otherHeaders = Seq("Environment" -> serviceEnvironment))

  def submitArrangement(taxpayer: Taxpayer, desSubmissionRequest: DesSubmissionRequest)(implicit ec: ExecutionContext): Future[Boolean] = {
    implicit val hc: HeaderCarrier = desHeaderCarrier

    val serviceUrl = s"time-to-pay/taxpayers/${taxpayer.selfAssessment.utr}/arrangements"

    http.POST[JsValue, HttpResponse](s"$desArrangementUrl/$serviceUrl", Json.toJson(desSubmissionRequest))
       .map( r => r.status match {
         case Status.ACCEPTED =>
           Logger.info(s"Submission successful for '${taxpayer.selfAssessment.utr}'")
           true
         case _ =>
           Logger.error(s"Arrangement submission failed for ${taxpayer.selfAssessment.utr}, response from DES [code: ${r.status}, body: ${r.body}]")
           false
       })
  }
}
