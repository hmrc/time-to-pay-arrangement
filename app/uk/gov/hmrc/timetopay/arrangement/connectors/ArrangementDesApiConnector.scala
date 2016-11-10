package uk.gov.hmrc.timetopay.arrangement.connectors

import play.api.Logger
import play.api.http.Status._
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

  val desHeaderCarrier: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(s"Bearer $authorisationToken")),
    otherHeaders = Seq("Environment" -> serviceEnvironment))

  def submitArrangement(taxpayer: Taxpayer, desSubmissionRequest: DesSubmissionRequest)(implicit ec: ExecutionContext): Future[SubmissionResult] = {
    implicit val hc: HeaderCarrier = desHeaderCarrier

    val serviceUrl = s"time-to-pay/taxpayers/${taxpayer.selfAssessment.utr}/arrangements"

    http.POST[DesSubmissionRequest, HttpResponse](s"$desArrangementUrl/$serviceUrl", desSubmissionRequest)
      .map(response => response.status match {
        case ACCEPTED =>
          Logger.info(s"Submission successful for '${taxpayer.selfAssessment.utr}'")
          Right(SubmissionSuccess(desSubmissionRequest))
        case INTERNAL_SERVER_ERROR | SERVICE_UNAVAILABLE =>
          Left(SubmissionError("Service call failed"))
        case _ =>
          Logger.error(s"Response code from DES ${response.status}")
          Left(SubmissionError(response.body))
      })
  }
}
