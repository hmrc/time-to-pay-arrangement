/*
 * Copyright 2017 HM Revenue & Customs
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

import play.api.Logger
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.timetopay.arrangement._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class TTPArrangementService(arrangementDesApiConnector: ((Taxpayer, DesSubmissionRequest) => Future[Either[SubmissionError, SubmissionSuccess]]),
                            desTTPArrangementService: (TTPArrangement => DesTTPArrangement),
                            letterAndControlService: (TTPArrangement => LetterAndControl),
                            arrangementSave: (TTPArrangement => Future[Option[TTPArrangement]]),
                            arrangementGet: (String => Future[Option[TTPArrangement]])) {

  def byId(id: String): Future[Option[TTPArrangement]] = arrangementGet(id)

  def submit(arrangement: TTPArrangement)(implicit hc: HeaderCarrier): Future[Option[TTPArrangement]] = {
    Logger.info(s"Submitting ttp arrangement for DD '${arrangement.directDebitReference}' " +
      s"and PP '${arrangement.paymentPlanReference}'")

    val letterAndControl = letterAndControlService(arrangement)
    val desTTPArrangement = desTTPArrangementService(arrangement)

    val request: DesSubmissionRequest = DesSubmissionRequest(desTTPArrangement, letterAndControl)

    (for {
      response <- arrangementDesApiConnector(arrangement.taxpayer, request)
      ttp <- saveArrangement(arrangement, request)
    } yield (response, ttp)).flatMap {
      result =>
        result._1.fold(error => Future.failed(DesApiException(error.code, error.message)),
          success => Future.successful(result._2))
    }
  }

  private def saveArrangement(arrangement: TTPArrangement, desSubmissionRequest: DesSubmissionRequest): Future[Option[TTPArrangement]] = {
    val toSave = arrangement.copy(id = Some(UUID.randomUUID().toString),
      createdOn = Some(LocalDateTime.now()),
      desArrangement = Some(desSubmissionRequest))

    Try(arrangementSave(toSave)).getOrElse(Future.successful(None))
  }

}

case class DesApiException(code: Int, message: String) extends RuntimeException(s"DES httpCode: $code, reason: $message") {}
