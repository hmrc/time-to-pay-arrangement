package uk.gov.hmrc.timetopay.arrangement.services

import play.api.Logger
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.Authorization
import uk.gov.hmrc.timetopay.arrangement.modelFormat._
import uk.gov.hmrc.timetopay.arrangement.{DesSubmissionRequest, Taxpayer}

import scala.concurrent.{ExecutionContext, Future}

case class SubmissionSuccess()

case class SubmissionError(code: Int, message: String)

trait DesArrangementService {

  type SubmissionResult = Either[SubmissionError, SubmissionSuccess]

  val authorisationToken: String
  val serviceEnvironment: String
  val desArrangementUrl: String
  val http: HttpGet with HttpPost

  lazy val desHeaderCarrier: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(s"Bearer $authorisationToken")))
    .withExtraHeaders("Environment" -> serviceEnvironment)


  def submitArrangement(taxpayer: Taxpayer, desSubmissionRequest: DesSubmissionRequest)(implicit ec: ExecutionContext): Future[SubmissionResult] = {
    implicit val hc: HeaderCarrier = desHeaderCarrier

    val serviceUrl = s"time-to-pay/taxpayers/${taxpayer.selfAssessment.utr}/arrangements"

    Logger.debug(s"Header carrier ${hc.headers}")
    Logger.debug(s"Request sent to DES ${Json.prettyPrint(Json.toJson(desSubmissionRequest))}")

    http.POST[DesSubmissionRequest, HttpResponse](s"$desArrangementUrl/$serviceUrl", desSubmissionRequest)
      .map(_ => {
        Logger.info(s"Submission successful for '${taxpayer.selfAssessment.utr}'")
        Right(SubmissionSuccess())
      }).recover {
      case e: Throwable => onError(e)
    }
  }

  private def onError(ex: Throwable) = {
    val (code, message) = ex match {
      case e: HttpException => (e.responseCode, e.getMessage)

      case e: Upstream4xxResponse => (e.reportAs, e.getMessage)
      case e: Upstream5xxResponse => (e.reportAs, e.getMessage)

      case e: Throwable => (Status.INTERNAL_SERVER_ERROR, e.getMessage)
    }

    Logger.error(s"Failure from DES, code $code and body $message")
    Left(SubmissionError(code, message))
  }

}

