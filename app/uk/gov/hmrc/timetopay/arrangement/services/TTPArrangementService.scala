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

package uk.gov.hmrc.timetopay.arrangement.services

import java.time.{Clock, Duration, Instant, LocalDateTime}
import javax.inject.Inject
import play.api.mvc.Request
import uk.gov.hmrc.timetopay.arrangement.config.{QueueConfig, QueueLogger}
import uk.gov.hmrc.timetopay.arrangement.connectors.{DesArrangementApiServiceConnector, SubmissionError, SubmissionSuccess}
import uk.gov.hmrc.timetopay.arrangement.model.{DesSubmissionRequest, AnonymousTTPArrangement, TTPArrangement, TTPArrangementWorkItem}
import uk.gov.hmrc.timetopay.arrangement.repository.{TTPArrangementRepository, TTPArrangementWorkItemRepository}
import uk.gov.hmrc.mongo.workitem.WorkItem

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class TTPArrangementService @Inject() (
    desTTPArrangementBuilder:         DesTTPArrangementBuilder,
    desArrangementApiService:         DesArrangementApiServiceConnector,
    ttpArrangementRepository:         TTPArrangementRepository,
    ttpArrangementRepositoryWorkItem: TTPArrangementWorkItemRepository,
    auditService:                     AuditService,
    val clock:                        Clock,
    letterAndControlBuilder:          LetterAndControlBuilder,
    crypto:                           CryptoService,
    queueConfig:                      QueueConfig) {
  val logger: QueueLogger = QueueLogger(getClass)

  val CLIENT_CLOSED_REQUEST = 499 // Client closes the connection while nginx is processing the request.

  def byId(id: String): Future[Option[AnonymousTTPArrangement]] = ttpArrangementRepository.findById(id)

  /**
   * Builds and submits the TTPArrangement to Des. Also saves to Mongo
   */
  def submit(arrangement: TTPArrangement)(implicit r: Request[_]): Future[Option[AnonymousTTPArrangement]] = {
    logger.trace(arrangement, s"Submitting ttp arrangement for DD '${arrangement.directDebitReference}' " +
      s"and PP '${arrangement.paymentPlanReference}'")

    val letterAndControl = letterAndControlBuilder.create(arrangement)
    val desTTPArrangement = desTTPArrangementBuilder.create(arrangement)

    val request: DesSubmissionRequest = DesSubmissionRequest(desTTPArrangement, letterAndControl)
    val utr = arrangement.taxpayer.selfAssessment.utr

    (for {
      response <- desArrangementApiService.submitArrangement(utr, request)
      ttp <- saveArrangement(arrangement, request)
    } yield {
      response match {
        case error: SubmissionError =>
          logger.trace(arrangement, "des failed: code: " + error.code.toString + " msg: " + error.message)
          val isSeverError = (error.code >= CLIENT_CLOSED_REQUEST) || (error.code == 408)
          val returnedError = Future.failed(DesApiException(error.code, error.message))
          if (isSeverError) {
            logger.trace(arrangement, "des failed: adding to queue ")
            sendToTTPArrangementWorkRepo(
              utr,
              affixDesArrangement(arrangement, request)).flatMap { _ =>
                returnedError
              }
          } else {
            logger.trace(arrangement, "des failed: NOT adding to queue ")
            returnedError
          }

        case _: SubmissionSuccess =>
          Future.successful {
            logger.trace(arrangement, "successful sent to des")
            auditService.sendSubmissionSucceededEvent(arrangement.taxpayer, arrangement.bankDetails, arrangement.schedule, AuditService.auditTags)
            ttp
          }
      }
    }).flatten
  }

  /**
   * Saves the TTPArrangement to our mongoDB and adds in a Id
   */
  private def saveArrangement(arrangement: TTPArrangement, desSubmissionRequest: DesSubmissionRequest): Future[Option[AnonymousTTPArrangement]] = {
    val toSave = affixDesArrangement(arrangement, desSubmissionRequest)

    Try(ttpArrangementRepository.doInsert(toSave)).getOrElse(Future.successful(None))
  }

  def affixDesArrangement(arrangement: TTPArrangement, desSubmissionRequest: DesSubmissionRequest): TTPArrangement = {
    arrangement.copy(desArrangement = Some(desSubmissionRequest))
  }

  private def sendToTTPArrangementWorkRepo(
      utr:         String,
      arrangement: TTPArrangement
  )(implicit request: Request[_]): Future[WorkItem[TTPArrangementWorkItem]] = {
    val time: LocalDateTime = LocalDateTime.now(clock)
    val availableUntil = time.plus(Duration.ofMillis(queueConfig.availableFor.toMillis))

    val instantNow: Instant = ttpArrangementRepositoryWorkItem.now

    logger.trace(utr, "Item to add to workItem")

    ttpArrangementRepositoryWorkItem.pushNew(
      TTPArrangementWorkItem(
        time,
        availableUntil,
        utr,
        crypto.encryptTtpa(arrangement),
        crypto.encryptAuditTags(AuditService.auditTags)
      ), instantNow
    ).map{ workItem: WorkItem[TTPArrangementWorkItem] =>
        logger.traceWorkItem(utr, workItem, "Pushed to work queue ")
        workItem
      }
  }
}

case class DesApiException(code: Int, message: String) extends RuntimeException(s"DES httpCode: $code, reason: $message") {}
