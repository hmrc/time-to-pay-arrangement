/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{Authorization, HttpClient, _}
import uk.gov.hmrc.timetopay.arrangement.config.DesArrangementApiServiceConnectorConfig
import uk.gov.hmrc.timetopay.arrangement.model.DesSubmissionRequest
import uk.gov.hmrc.timetopay.arrangement.model.modelFormat._

import scala.concurrent.{ExecutionContext, Future}

case class SubmissionSuccess()

case class SubmissionError(code: Int, message: String)

object SubmissionError {
  implicit val format: OFormat[SubmissionError] = Json.format[SubmissionError]
}

@Singleton
class DesArrangementApiServiceConnector @Inject() (
    httpClient: HttpClient,
    config:     DesArrangementApiServiceConnectorConfig
) {
  val logger: Logger = Logger(getClass)

  type SubmissionResult = Either[SubmissionError, SubmissionSuccess]

  // external services require explicitly passed headers
  private implicit val emptyHc: HeaderCarrier = HeaderCarrier()
  private val headers: Seq[(String, String)] = config.desHeaders

  def submitArrangement(
      utr:                  String,
      desSubmissionRequest: DesSubmissionRequest
  )(implicit ec: ExecutionContext): Future[SubmissionResult] = {
    //we put sessionId and requestId into hc so auditor can populate these fields when auditing
    //request to DES

    val serviceUrl = s"time-to-pay/taxpayers/${utr}/arrangements"

    httpClient.POST[DesSubmissionRequest, HttpResponse](
      s"${config.desArrangementUrl}/$serviceUrl",
      desSubmissionRequest, headers = headers)
      .map {
        case res if res.status == Status.ACCEPTED =>
          logger.info(s"Submission successful for '${utr}'")
          Right(SubmissionSuccess())

        case res => Left(SubmissionError(res.status, res.body))
      }
  }

}
