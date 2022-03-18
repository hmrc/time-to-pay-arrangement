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

import jdk.nashorn.api.scripting.JSObject

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsObject, JsString, Json, OFormat}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{Authorization, HttpClient, _}
import uk.gov.hmrc.timetopay.arrangement.config.{DesArrangementApiServiceConnectorConfig, QueueLogger}
import uk.gov.hmrc.timetopay.arrangement.model.DesSubmissionRequest
import uk.gov.hmrc.timetopay.arrangement.model.modelFormat._

import scala.concurrent.{ExecutionContext, Future}

class SubmissionResult

case class SubmissionSuccess() extends SubmissionResult

case class SubmissionError(code: Int, message: String) extends SubmissionResult

object SubmissionError {
  implicit val format: OFormat[SubmissionError] = Json.format[SubmissionError]
}

@Singleton
class DesArrangementApiServiceConnector @Inject() (
    httpClient: HttpClient,
    config:     DesArrangementApiServiceConnectorConfig
) {
  val logger: Logger = Logger(getClass)
  private val zonkLogger = QueueLogger(this.getClass)

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
          zonkLogger.trace(utr, "DES POST OK " + res.toString())
          SubmissionSuccess()

        case res =>
          zonkLogger.trace(utr, "DES POST Failed " + res.toString())
          logger.info(s"Submission FAILED for '${utr}'")
          if (res.status == Status.BAD_REQUEST) {
            logger.error(s"BadRequest for '${utr}' json: " + redaction(desSubmissionRequest))
          }
          SubmissionError(res.status, res.body)
      }.recover {
        case _ =>
          zonkLogger.trace(utr, "DES POST Failed TIMEOUT")
          logger.info(s"Submission FAILED for '${utr}'")
          SubmissionError(599, "network timeout exception")
      }
  }

  private def redaction(desSubmissionRequest: DesSubmissionRequest): String = {
    val obj = Json.toJson(desSubmissionRequest).as[JsObject]

    val letterAndControlKeys = List(
      "customerName",
      "salutation",
      "addressLine1",
      "addressLine2",
      "addressLine3",
      "addressLine4",
      "addressLine5",
      "clmPymtString",
      "postCode",
      "totalAll"
    )

    val ttpArrangementKeys = List(
      "firstPaymentAmount",
      "regularPaymentAmount",
      "saNote"
    )

    val obj1 = obfuscate(obj, "ttpArrangement", ttpArrangementKeys)
    val obj2 = obfuscate(obj1, "letterAndControl", letterAndControlKeys)

    Json.prettyPrint(obj2)
  }

  def obfuscate(obj: JsObject, key: String, fields: List[String]): JsObject = {
      def crypt(s: String): String = {
        s.replaceAll("[a-zA-Z]", "X").replaceAll("[0-9]", "9")
      }

    val section = (obj \ key).as[JsObject]

    obj ++ Json.obj(key -> fields.foldLeft(section){
      (l, k) =>
        (l \ k).asOpt[String].fold(l){
          _ => l ++ Json.obj(k -> crypt((section \ k).as[String]))
        }
    })
  }

}
