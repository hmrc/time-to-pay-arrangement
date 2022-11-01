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
import java.util.UUID
import javax.inject.Inject
import play.api.libs.json.JsValue
import play.api.mvc.Request
import uk.gov.hmrc.timetopay.arrangement.config.{QueueConfig, QueueLogger}
import uk.gov.hmrc.timetopay.arrangement.connectors.{DesArrangementApiServiceConnector, SubmissionError, SubmissionSuccess}
import uk.gov.hmrc.timetopay.arrangement.model.{AnonymisedDesSubmissionRequest, AnonymisedSelfAssessment, AnonymisedTaxpayer, DesSubmissionRequest, LetterAndControl, SelfAssessment, TTPAnonymisedArrangement, TTPArrangement, TTPArrangementWorkItem, Taxpayer}
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
  val logger = QueueLogger(getClass)

  val CLIENT_CLOSED_REQUEST = 499 // Client closes the connection while nginx is processing the request.

  def byId(id: String): Future[Option[TTPAnonymisedArrangement]] = ttpArrangementRepository.findByIdLocal(id)

  /**
   * Builds and submits the TTPArrangement to Des. Also saves to Mongo
   */
  def submit(arrangement: TTPArrangement)(implicit r: Request[_]): Future[Option[TTPAnonymisedArrangement]] = {
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
              arrangementToSave(arrangement, request)).flatMap { _ =>
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
  private def saveArrangement(arrangement: TTPArrangement, desSubmissionRequest: DesSubmissionRequest): Future[Option[TTPAnonymisedArrangement]] = {
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

  def anonymiseArrangement(ttpArrangement: TTPArrangement): TTPAnonymisedArrangement = {
    TTPAnonymisedArrangement(
      _id                  = ttpArrangement.id.getOrElse(throw new RuntimeException("Found None")),
      createdOn            = ttpArrangement.createdOn.getOrElse(throw new RuntimeException("Found None")),
      paymentPlanReference = ttpArrangement.paymentPlanReference,
      directDebitReference = ttpArrangement.directDebitReference,
      taxpayer             = AnonymisedTaxpayer(
        selfAssessment = AnonymisedSelfAssessment(
          utr = ttpArrangement.taxpayer.selfAssessment.utr
        )
      ),
      bankDetails          = ttpArrangement.bankDetails,
      schedule             = ttpArrangement.schedule,
      desArrangement       = ttpArrangement.desArrangement match {
        case None => None
        case Some(desSubmissionRequest: DesSubmissionRequest) =>
          Some(
            AnonymisedDesSubmissionRequest(
              ttpArrangement = desSubmissionRequest.ttpArrangement
            )
          )
      }
    )
  }

  def padAnonymisedArrangement(anonymisedArrangement: TTPAnonymisedArrangement): TTPArrangement = {
    TTPArrangement(
      id                   = Some(anonymisedArrangement._id),
      createdOn            = Some(anonymisedArrangement.createdOn),
      paymentPlanReference = anonymisedArrangement.paymentPlanReference,
      directDebitReference = anonymisedArrangement.directDebitReference,
      taxpayer             = Taxpayer(
        customerName   = "",
        addresses      = List(),
        selfAssessment = SelfAssessment(
          utr                      = anonymisedArrangement.taxpayer.selfAssessment.utr,
          communicationPreferences = None,
          debits                   = List()
        )
      ),
      bankDetails          = anonymisedArrangement.bankDetails,
      schedule             = anonymisedArrangement.schedule,
      desArrangement       = anonymisedArrangement.desArrangement match {
        case None => None
        case Some(anonymisedDesSubmissionRequest) =>
          Some(
            DesSubmissionRequest(
              ttpArrangement   = anonymisedDesSubmissionRequest.ttpArrangement,
              letterAndControl = LetterAndControl(
                customerName  = "",
                salutation    = "",
                totalAll      = "",
                clmPymtString = ""
              )
            )
          )
      }
    )
  }

}

case class DesApiException(code: Int, message: String) extends RuntimeException(s"DES httpCode: $code, reason: $message") {}
