/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.timetopay.arrangement.connectors

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.http.Status
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.timetopay.arrangement.config.DesArrangementApiServiceConnectorConfig
import uk.gov.hmrc.timetopay.arrangement.modelFormat._
import uk.gov.hmrc.timetopay.arrangement.{DesSubmissionRequest, Taxpayer}

import scala.concurrent.{ExecutionContext, Future}

case class SubmissionSuccess()

case class SubmissionError(code: Int, message: String)

@Singleton
class DesArrangementApiServiceConnector @Inject() (
    httpClient: HttpClient,
    config:     DesArrangementApiServiceConnectorConfig)(implicit ec: ExecutionContext) {

  type SubmissionResult = Either[SubmissionError, SubmissionSuccess]

  lazy val desHeaderCarrier: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(s"Bearer ${config.authorisationToken}")))
    .withExtraHeaders("Environment" -> config.serviceEnvironment)

  def submitArrangement(taxpayer: Taxpayer, desSubmissionRequest: DesSubmissionRequest)(implicit ec: ExecutionContext): Future[SubmissionResult] = {
    implicit val hc: HeaderCarrier = desHeaderCarrier
    val serviceUrl = s"time-to-pay/taxpayers/${taxpayer.selfAssessment.utr}/arrangements"

    Logger.logger.debug(s"Header carrier ${hc.headers}")
    httpClient.POST[DesSubmissionRequest, HttpResponse](s"${config.desArrangementUrl}/$serviceUrl", desSubmissionRequest)
      .map(_ => {
        Logger.logger.info(s"Submission successful for '${taxpayer.selfAssessment.utr}'")
        Right(SubmissionSuccess())
      }).recover {
        case e: Throwable =>

          onError(e)

      }
  }

  private def onError(ex: Throwable): SubmissionResult = {
    val (code, message) = ex match {
      case e: HttpException       => (e.responseCode, e.getMessage)

      case e: Upstream4xxResponse => (e.reportAs, e.getMessage)
      case e: Upstream5xxResponse => (e.reportAs, e.getMessage)

      case e: Throwable           => (Status.INTERNAL_SERVER_ERROR, e.getMessage)
    }

    Logger.logger.error(s"Failure from DES, code $code and body $message")
    Left(SubmissionError(code, message))
  }
}
