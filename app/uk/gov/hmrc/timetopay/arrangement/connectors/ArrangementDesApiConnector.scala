package uk.gov.hmrc.timetopay.arrangement.connectors

import play.api.{Play, Logger}
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.play.http.logging.Authorization
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.timetopay.arrangement.models.{DesSubmissionRequest, Taxpayer}
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._
import scala.concurrent.{ExecutionContext, Future}


case class SubmissionSuccess(requestSent: DesSubmissionRequest) {}

case class SubmissionError(message: String) {}

trait ArrangementDesApiConnector {

  type SubmissionResult = Either[SubmissionError, SubmissionSuccess]

  val authorisationToken: String
  val serviceEnvironment: String
  val desArrangementUrl: String
  val http: HttpGet with HttpPost

  lazy val desHeaderCarrier: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(s"Bearer $authorisationToken")),
    otherHeaders = Seq("Environment" -> serviceEnvironment))

  def submitArrangement(taxpayer: Taxpayer, desSubmissionRequest: DesSubmissionRequest)(implicit ec: ExecutionContext): Future[SubmissionResult] = {
    Logger.info(s"Environment $serviceEnvironment")
    implicit val hc: HeaderCarrier = desHeaderCarrier

    val serviceUrl = s"time-to-pay/taxpayers/${taxpayer.selfAssessment.utr}/arrangements"
    Logger.debug(s"Request sent to DES ${Json.prettyPrint(Json.toJson(desSubmissionRequest))}")
    http.POST[DesSubmissionRequest, HttpResponse](s"$desArrangementUrl/$serviceUrl", desSubmissionRequest)
      .map(response => response.status match {
        case ACCEPTED =>
          Logger.info(s"Submission successful for '${taxpayer.selfAssessment.utr}'")
          Right(SubmissionSuccess(desSubmissionRequest))
        case _ =>
          Logger.error(s"Response code from DES ${response.status} and body ${response.body}")
          Left(SubmissionError(response.body))
      })
  }
}
