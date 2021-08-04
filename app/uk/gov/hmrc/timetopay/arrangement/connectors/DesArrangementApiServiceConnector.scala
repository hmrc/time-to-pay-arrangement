/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{Authorization, HttpClient, _}
import uk.gov.hmrc.timetopay.arrangement.config.DesArrangementApiServiceConnectorConfig
import uk.gov.hmrc.timetopay.arrangement.model.modelFormat._
import uk.gov.hmrc.timetopay.arrangement.model.{DesSubmissionRequest, Taxpayer}

import scala.concurrent.{ExecutionContext, Future}

case class SubmissionSuccess()

case class SubmissionError(code: Int, message: String)

@Singleton
class DesArrangementApiServiceConnector @Inject() (
    httpClient: HttpClient,
    config:     DesArrangementApiServiceConnectorConfig
) {
  val logger: Logger = Logger(getClass)

  type SubmissionResult = Either[SubmissionError, SubmissionSuccess]

  def submitArrangement(
      taxpayer:             Taxpayer,
      desSubmissionRequest: DesSubmissionRequest
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[SubmissionResult] = {
    //we put sessionId and requestId into hc so auditor can populate these fields when auditing
    //request to DES
    val desHc: HeaderCarrier =
      HeaderCarrier(
        sessionId     = hc.sessionId,
        requestId     = hc.requestId,
        authorization = Some(Authorization(s"Bearer ${config.authorisationToken}")),
        extraHeaders  = Seq("Environment" -> config.serviceEnvironment)
      )

    val serviceUrl = s"time-to-pay/taxpayers/${taxpayer.selfAssessment.utr}/arrangements"

    httpClient.POST[DesSubmissionRequest, HttpResponse](
      s"${config.desArrangementUrl}/$serviceUrl",
      desSubmissionRequest)(
        implicitly,
        implicitly,
        desHc,
        ec
      )
      .map {
        case res if res.status == Status.ACCEPTED =>
          logger.info(s"Submission successful for '${taxpayer.selfAssessment.utr}'")
          Right(SubmissionSuccess())

        case res => Left(SubmissionError(res.status, res.body))
      }
  }

}
