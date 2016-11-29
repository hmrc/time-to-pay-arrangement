package uk.gov.hmrc.timetopay.arrangement.services

import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.play.http.logging.Authorization
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.timetopay.arrangement.{DesSubmissionRequest, Taxpayer}
import uk.gov.hmrc.timetopay.arrangement.modelFormat._

import scala.concurrent.{ExecutionContext, Future}

case class SubmissionSuccess()

case class SubmissionError(code: Int, message: String)

trait DesArrangementService {

  type SubmissionResult = Either[SubmissionError, SubmissionSuccess]

  val authorisationToken: String
  val serviceEnvironment: String
  val desArrangementUrl: String
  val http: HttpGet with HttpPost

  lazy val desHeaderCarrier: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(s"Bearer $authorisationToken")),
    otherHeaders = Seq("Environment" -> serviceEnvironment))

  def submitArrangement(taxpayer: Taxpayer, desSubmissionRequest: DesSubmissionRequest)(implicit ec: ExecutionContext): Future[SubmissionResult] = {
    implicit val hc: HeaderCarrier = desHeaderCarrier

    val serviceUrl = s"time-to-pay/taxpayers/${taxpayer.selfAssessment.utr}/arrangements"

    Logger.debug(s"Header carrier ${hc.headers}")
    Logger.debug(s"Request sent to DES ${Json.prettyPrint(Json.toJson(desSubmissionRequest))}")

    http.POST[DesSubmissionRequest, HttpResponse](s"$desArrangementUrl/$serviceUrl", desSubmissionRequest)
      .map(response => response.status match {
        case ACCEPTED =>
          Logger.info(s"Submission successful for '${taxpayer.selfAssessment.utr}'")
          Right(SubmissionSuccess())
        case _ =>
          Logger.error(s"Failure from DES, code ${response.status} and body ${response.body}")
          Left(SubmissionError(response.status, response.body))
      })
  }
}
