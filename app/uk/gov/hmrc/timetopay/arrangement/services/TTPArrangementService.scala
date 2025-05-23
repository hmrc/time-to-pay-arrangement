/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.mvc.Request
import uk.gov.hmrc.mongo.workitem.WorkItem
import uk.gov.hmrc.timetopay.arrangement.config.{QueueConfig, QueueLogger}
import uk.gov.hmrc.timetopay.arrangement.connectors.{DesArrangementApiServiceConnector, SubmissionError, SubmissionSuccess}
import uk.gov.hmrc.timetopay.arrangement.model.{DesSubmissionRequest, TTPArrangement, TTPArrangementWorkItem}
import uk.gov.hmrc.timetopay.arrangement.repository.TTPArrangementWorkItemRepository

import java.time.{Clock, Duration, Instant, LocalDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TTPArrangementService @Inject() (
    desTTPArrangementBuilder:         DesTTPArrangementBuilder,
    desArrangementApiService:         DesArrangementApiServiceConnector,
    ttpArrangementRepositoryWorkItem: TTPArrangementWorkItemRepository,
    auditService:                     AuditService,
    val clock:                        Clock,
    letterAndControlBuilder:          LetterAndControlBuilder,
    crypto:                           CryptoService,
    queueConfig:                      QueueConfig)(
    implicit
    ec: ExecutionContext
) {
  val logger: QueueLogger = QueueLogger(getClass)

  val CLIENT_CLOSED_REQUEST = 499 // Client closes the connection while nginx is processing the request.

  /**
   * Builds and submits the TTPArrangement to Des
   */
  def submit(arrangement: TTPArrangement)(implicit r: Request[_]): Future[TTPArrangement] = {
    logger.trace(arrangement, s"Submitting ttp arrangement for DD '${arrangement.directDebitReference}' " +
      s"and PP '${arrangement.paymentPlanReference}'")

    val letterAndControl = letterAndControlBuilder.create(arrangement)
    val desTTPArrangement = desTTPArrangementBuilder.create(arrangement)

    val request: DesSubmissionRequest = DesSubmissionRequest(desTTPArrangement, letterAndControl)
    val utr = arrangement.taxpayer.selfAssessment.utr

    (for {
      response <- desArrangementApiService.submitArrangement(utr, request)
      ttp <- buildArrangement(arrangement, request)
    } yield {
      response match {
        case error: SubmissionError =>
          logger.trace(arrangement, "des failed: code: " + error.code.toString + " msg: " + error.message)
          val isServerError = (error.code >= CLIENT_CLOSED_REQUEST) || (error.code == 408)
            def returnedError(queuedForRetry: Boolean): Future[Nothing] = {
              Future.failed(DesApiException(error.code, error.message, queuedForRetry))
            }
          if (isServerError) {
            logger.trace(arrangement, "des failed: adding to queue ")
            sendToTTPArrangementWorkRepo(
              utr,
              affixDesArrangement(arrangement, request)
            ).flatMap { workItem: WorkItem[TTPArrangementWorkItem] =>
                auditService.sendArrangementQueuedEvent(
                  arrangement,
                  error,
                  workItem.item,
                  AuditService.auditTags
                )

                returnedError(queuedForRetry = true)
              }
          } else {
            logger.trace(arrangement, "des failed: NOT adding to queue ")
            returnedError(queuedForRetry = false)
          }

        case _: SubmissionSuccess =>
          Future.successful {
            logger.trace(arrangement, "successful sent to des")
            auditService.sendSubmissionSucceededEvent(arrangement, AuditService.auditTags)
            ttp
          }
      }
    }).flatten
  }

  /**
   * Affixes desSubmissionRequest to arrangement and adds Id to TTPArrangement
   */
  private def buildArrangement(arrangement: TTPArrangement, desSubmissionRequest: DesSubmissionRequest): Future[TTPArrangement] = {
    val arrangementWithDesSubmissionRequest = affixDesArrangement(arrangement, desSubmissionRequest)
    Future.successful(arrangementWithDesSubmissionRequest)
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

    val instantNow: Instant = ttpArrangementRepositoryWorkItem.now()

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

case class DesApiException(code: Int, message: String, queuedForRetry: Boolean) extends RuntimeException(s"DES httpCode: ${code.toString}, reason: $message") {}
