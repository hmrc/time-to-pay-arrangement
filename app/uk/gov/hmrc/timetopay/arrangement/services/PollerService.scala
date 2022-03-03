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

import akka.actor.{ActorSystem, Cancellable}
import com.google.inject.Singleton
import play.api.Logger
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.scheduling.ExclusiveScheduledJob
import uk.gov.hmrc.timetopay.arrangement.config.{QueueConfig, QueueLogger}
import uk.gov.hmrc.timetopay.arrangement.connectors.{DesArrangementApiServiceConnector, SubmissionError, SubmissionSuccess}
import uk.gov.hmrc.timetopay.arrangement.model.{DesSubmissionRequest, TTPArrangementWorkItem}
import uk.gov.hmrc.timetopay.arrangement.repository.TTPArrangementWorkItemRepository
import uk.gov.hmrc.workitem.{Failed, PermanentlyFailed, WorkItem}

import java.time.{Clock, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class PollerService @Inject() (
    actorSystem:                       ActorSystem,
    desArrangementApiServiceConnector: DesArrangementApiServiceConnector,
    ttpArrangementRepositoryWorkItem:  TTPArrangementWorkItemRepository,
    queueConfig:                       QueueConfig,
    crypto:                            CryptoService,
    auditService:                      AuditService,
    val clock:                         Clock)(
    implicit
    ec: ExecutionContext
) extends ExclusiveScheduledJob {

  private val logger = QueueLogger(this.getClass)
  val initialDelay = queueConfig.initialDelay
  val interval = queueConfig.interval

  override def executeInMutex(implicit ec: ExecutionContext): Future[Result] = {
    ttpArrangementRepositoryWorkItem.find().map{ ls =>
      val txt = ls.map(x => x.status).groupBy(x => x).map{ x => s"${x._1}: ${x._2.size}" }.mkString(", ")
      logger.track("Retry poller - WorkItem counts- " + txt)
    }

    process().map(_ => Result(""))
  }

  override def name: String = "TTP Arrangement Poller"
  callExecutor(name)
  logger.track("Retry poller Started: " + name)
  def callExecutor(name: String)(implicit ec: ExecutionContext): Cancellable = {
    actorSystem.scheduler.scheduleWithFixedDelay(initialDelay, interval)(() => {
      logger.track("Retry poller start " + name)
      executor(name)
    })
  }

  def executor(name: String)(implicit ec: ExecutionContext): Unit = {
    execute.onComplete({
      case Success(Result(res)) =>
        logger.track("Retry poller end - " + res)
      case Failure(throwable) =>
        logger.error(s"Retry poller $name: Exception completing work item", throwable)
    })
  }

  def isAvailable(workItem: TTPArrangementWorkItem): Boolean = {
    val time = LocalDateTime.now(clock)
    time.isBefore(workItem.availableUntil)
  }

  def tryDesCallAgain(wi: WorkItem[TTPArrangementWorkItem], finalAttempt: Boolean): Future[Unit] = {
    try {
      val arrangment = crypto.decryptTtpa(wi.item.ttpArrangement)
        .getOrElse(throw new RuntimeException("Retry poller - Saved ttp in work item repo had invalid encrypted item " + wi.toString))
      val auditTags = crypto.decryptAuditTags(wi.item.auditTags)
        .getOrElse(throw new RuntimeException("Retry poller - Saved ttp in work item repo had invalid encrypted audit tags " + wi.toString))

      val utr = arrangment.taxpayer.selfAssessment.utr
      logger.trace(utr, "Retry poller - retry send to DES")
      val desSubmissionRequest: DesSubmissionRequest = arrangment.desArrangement
        .getOrElse(throw new RuntimeException("Retry poller - Saved ttp in work item repo had no desArrangement to send " + wi.toString))

      desArrangementApiServiceConnector.submitArrangement(utr, desSubmissionRequest).map {
        case _: SubmissionSuccess =>
          logger.trace(utr, "Retry poller - SUCCESSFULLY send to DES")

          auditService.sendSubmissionSucceededEvent(arrangment.taxpayer, arrangment.bankDetails, arrangment.schedule, auditTags)

          ttpArrangementRepositoryWorkItem.complete(wi.id).map(_ => process())
          ()
        case error: SubmissionError =>
          if (finalAttempt) {
            logger.error("Retry poller - ZONK ERROR! Call failed and will not be tried again for " + wi.toString)

            auditService.sendArrangementSubmissionFailedEvent(arrangment.taxpayer, arrangment.bankDetails, arrangment.schedule, error, auditTags)
            ttpArrangementRepositoryWorkItem.markAs(wi.id, PermanentlyFailed, None).flatMap(_ => process())
          } else {
            logger.traceWorkItem(utr, wi, "Retry poller - Call failed. reason: " + error.toString)

            ttpArrangementRepositoryWorkItem.markAs(wi.id, Failed, None).map(_ => process())
          }

          ()
      }
    } catch {
      case err: Throwable =>
        logger.error("Retry poller - DES problem", err)
        throw err
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def process(): Future[Unit] = {
    try {

      ttpArrangementRepositoryWorkItem.pullOutstanding
        .flatMap {
          case None =>
            Future.successful(())
          case Some(wi) =>
            val finalAttempt = !isAvailable(wi.item)

            logger.traceWorkItem("Unknown", wi, "Retry poller - Retrying call to des api ")
            tryDesCallAgain(wi, finalAttempt)
        }
    } catch {
      case err: Throwable =>
        logger.error("Retry poller - process outstanding workitems problem", err)
        throw err
    }
  }

}

