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

package uk.gov.hmrc.timetopay.arrangement.services

import java.time.{Clock, Duration, LocalDateTime, ZoneId}
import java.util.UUID
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.JsValue
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.timetopay.arrangement.config.QueueConfig
import uk.gov.hmrc.timetopay.arrangement.connectors.DesArrangementApiServiceConnector
import uk.gov.hmrc.timetopay.arrangement.model.{DesSubmissionRequest, TTPArrangement, TTPArrangementWorkItem}
import uk.gov.hmrc.timetopay.arrangement.repository.{TTPArrangementRepository, TTPArrangementWorkItemRepository}
import uk.gov.hmrc.workitem.WorkItem

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class TTPArrangementService @Inject() (
    desTTPArrangementBuilder:         DesTTPArrangementBuilder,
    desArrangementApiService:         DesArrangementApiServiceConnector,
    ttpArrangementRepository:         TTPArrangementRepository,
    ttpArrangementRepositoryWorkItem: TTPArrangementWorkItemRepository,
    val clock:                        Clock,
    letterAndControlBuilder:          LetterAndControlBuilder,
    crypto:                           CryptoService,
    queueConfig:                      QueueConfig) {
  val logger: Logger = Logger(getClass)

  def byId(id: String): Future[Option[JsValue]] = ttpArrangementRepository.findByIdLocal(id)

  /**
   * Builds and submits the TTPArrangement to Des. Also saves to Mongo
   */
  def submit(arrangement: TTPArrangement)(implicit r: Request[_]): Future[Option[TTPArrangement]] = {
    logger.info(s"Submitting ttp arrangement for DD '${arrangement.directDebitReference}' " +
      s"and PP '${arrangement.paymentPlanReference}'")

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(r)

    val letterAndControl = letterAndControlBuilder.create(arrangement)
    val desTTPArrangement = desTTPArrangementBuilder.create(arrangement)

    val request: DesSubmissionRequest = DesSubmissionRequest(desTTPArrangement, letterAndControl)
    val utr = arrangement.taxpayer.selfAssessment.utr
    (for {
      response <- desArrangementApiService.submitArrangement(utr, request)
      ttp <- saveArrangement(arrangement, request)
    } yield (response, ttp)).flatMap {
      result =>
        result._1.fold(
          error => {
            val isSeverError = error.code >= Status.INTERNAL_SERVER_ERROR
            val returnedError = Future.failed(DesApiException(error.code, error.message))
            if (isSeverError) {
              sendToTTPArrangementWorkRepo(
                utr,
                arrangementToSave(arrangement, request)).flatMap { _ =>
                  returnedError
                }
            } else
              returnedError
          },
          _ => Future.successful(result._2))
    }
  }

  /**
   * Saves the TTPArrangement to our mongoDB and adds in a Id
   */
  private def saveArrangement(arrangement: TTPArrangement, desSubmissionRequest: DesSubmissionRequest): Future[Option[TTPArrangement]] = {
    val toSave = arrangementToSave(arrangement, desSubmissionRequest)

    Try(ttpArrangementRepository.doInsert(toSave)).getOrElse(Future.successful(None))
  }

  def arrangementToSave(arrangement: TTPArrangement, desSubmissionRequest: DesSubmissionRequest): TTPArrangement = {
    arrangement.copy(
      id             = Some(UUID.randomUUID().toString),
      createdOn      = Some(LocalDateTime.now()),
      desArrangement = Some(desSubmissionRequest))
  }

  private def sendToTTPArrangementWorkRepo(
      utr:         String,
      arrangement: TTPArrangement
  )(implicit request: Request[_]): Future[WorkItem[TTPArrangementWorkItem]] = {
    val time: LocalDateTime = LocalDateTime.now(clock)
    val availableUntil = time.plus(Duration.ofMillis(queueConfig.availableFor.toMillis))

    val jodaLocalDateTime: DateTime = ttpArrangementRepositoryWorkItem.now

    ttpArrangementRepositoryWorkItem.pushNew(
      TTPArrangementWorkItem(
        time,
        availableUntil,
        utr,
        crypto.encryptTtpa(arrangement),
        crypto.encryptAuditTags(AuditService.auditTags)
      ), jodaLocalDateTime
    )
  }

}

case class DesApiException(code: Int, message: String) extends RuntimeException(s"DES httpCode: $code, reason: $message") {}
