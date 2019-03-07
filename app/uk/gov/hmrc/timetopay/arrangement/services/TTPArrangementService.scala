/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.timetopay.arrangement.services

import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

import play.api.Logger
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.timetopay.arrangement._
import uk.gov.hmrc.timetopay.arrangement.config.DesArrangementApiService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class TTPArrangementService @Inject()(desTTPArrangementBuilder: DesTTPArrangementBuilder,
                                      desArrangementApiService: DesArrangementApiService,
                                      ttpArrangementRepository: TTPArrangementRepository,
                                      letterAndControlBuilder: LetterAndControlBuilder) {

  def byId(id: String): Future[Option[JsValue]] = ttpArrangementRepository.findByIdLocal(id)

  /**
    * Builds and submits the TTPArrangement to Des. Also saves to Mongo
    */
  def submit(arrangement: TTPArrangement)(implicit hc: HeaderCarrier): Future[Option[TTPArrangement]] = {
    Logger.logger.info(s"Submitting ttp arrangement for DD '${arrangement.directDebitReference}' " +
      s"and PP '${arrangement.paymentPlanReference}'")

    val letterAndControl = letterAndControlBuilder.create(arrangement)
    val desTTPArrangement = desTTPArrangementBuilder.create(arrangement)

    val request: DesSubmissionRequest = DesSubmissionRequest(desTTPArrangement, letterAndControl)
    (for {
      response <- desArrangementApiService.submitArrangement(arrangement.taxpayer, request)
      ttp <- saveArrangement(arrangement, request)
    } yield (response, ttp)).flatMap {
      result =>
        result._1.fold(error => Future.failed(DesApiException(error.code, error.message)),
          success => Future.successful(result._2))
    }
  }

  /**
    * Saves the TTPArrangement to our mongoDB and adds in a Id
    */
  private def saveArrangement(arrangement: TTPArrangement, desSubmissionRequest: DesSubmissionRequest): Future[Option[TTPArrangement]] = {
    val toSave = arrangement.copy(id = Some(UUID.randomUUID().toString),
      createdOn = Some(LocalDateTime.now()),
      desArrangement = Some(desSubmissionRequest))

    Try(ttpArrangementRepository.save(toSave)).getOrElse(Future.successful(None))
  }

}

case class DesApiException(code: Int, message: String) extends RuntimeException(s"DES httpCode: $code, reason: $message") {}
