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

import akka.actor.Scheduler
import com.google.inject.Singleton
import uk.gov.hmrc.mongo.workitem.ProcessingStatus.{Failed, PermanentlyFailed}
import uk.gov.hmrc.timetopay.arrangement.config.{QueueConfig, QueueLogger}
import uk.gov.hmrc.timetopay.arrangement.connectors.{DesArrangementApiServiceConnector, SubmissionError, SubmissionSuccess}
import uk.gov.hmrc.timetopay.arrangement.model.{DesSubmissionRequest, TTPArrangementWorkItem}
import uk.gov.hmrc.timetopay.arrangement.repository.TTPArrangementWorkItemRepository
import uk.gov.hmrc.mongo.workitem.WorkItem
import uk.gov.hmrc.timetopay.arrangement.services.PollerService.OnCompleteAction

import java.time.{Clock, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class PollerService @Inject() (
    desArrangementApiServiceConnector: DesArrangementApiServiceConnector,
    ttpArrangementRepositoryWorkItem:  TTPArrangementWorkItemRepository,
    queueConfig:                       QueueConfig,
    crypto:                            CryptoService,
    auditService:                      AuditService,
    val clock:                         Clock,
    scheduler:                         Scheduler,
    onCompleteAction:                  OnCompleteAction
)(
    implicit
    ec: ExecutionContext
) {

  private val logger = QueueLogger(this.getClass)
  val initialDelay: FiniteDuration = queueConfig.initialDelay
  val interval: FiniteDuration = queueConfig.interval

  val name: String = "TTP Arrangement Poller"

  private def scheduleNext(interval: FiniteDuration): Unit = {
    val _ = scheduler.scheduleOnce(interval) {
      logger.track("Retry poller start " + name)
      execute()
    }
  }

  scheduleNext(initialDelay)

  logger.track("Retry poller Started: " + name)

  private def execute(): Unit = {
    val result = ttpArrangementRepositoryWorkItem.findAll().flatMap { ls =>
      if (ls.isEmpty)
        logger.track("Retry poller - No WorkItems to handle")
      else {
        val txt = ls.map(x => x.status).groupBy(x => x).map { x => s"${x._1.toString}: ${x._2.size.toString}" }.mkString(", ")
        logger.track("Retry poller - WorkItem counts- " + txt)
      }
      process()
    }

    result.onComplete{ result =>
      result match {
        case Success(()) =>
          logger.track("Retry poller end - ")
        case Failure(throwable) =>
          logger.error(s"Retry poller $name: Exception completing work item", throwable)
      }
      scheduleNext(interval)
      onCompleteAction.onComplete()
    }
  }

  private def isAvailable(workItem: TTPArrangementWorkItem): Boolean = {
    val time = LocalDateTime.now(clock)
    time.isBefore(workItem.availableUntil)
  }

  private def tryDesCallAgain(wi: WorkItem[TTPArrangementWorkItem], finalAttempt: Boolean): Future[Unit] = {
    try {
      val arrangement = crypto.decryptTtpa(wi.item.ttpArrangement)
        .getOrElse(throw new RuntimeException("Retry poller - Saved ttp in work item repo had invalid encrypted item " + wi.toString))
      val auditTags = crypto.decryptAuditTags(wi.item.auditTags)
        .getOrElse(throw new RuntimeException("Retry poller - Saved ttp in work item repo had invalid encrypted audit tags " + wi.toString))

      val utr = arrangement.taxpayer.selfAssessment.utr
      logger.trace(utr, "Retry poller - retry send to DES")
      val desSubmissionRequest: DesSubmissionRequest = arrangement.desArrangement
        .getOrElse(throw new RuntimeException("Retry poller - Saved ttp in work item repo had no desArrangement to send " + wi.toString))

      desArrangementApiServiceConnector.submitArrangement(utr, desSubmissionRequest).flatMap {
        case _: SubmissionSuccess =>
          logger.trace(utr, "Retry poller - SUCCESSFULLY send to DES")

          auditService.sendSubmissionSucceededEvent(arrangement, auditTags)

          ttpArrangementRepositoryWorkItem.completeAndDelete(wi.id).flatMap(_ => process())
        case error: SubmissionError =>
          if (finalAttempt) {
            logger.error("Retry poller - ZONK ERROR! Call failed and will not be tried again for " + wi.toString)

            auditService.sendArrangementSubmissionFailedEvent(arrangement, error, auditTags)
            ttpArrangementRepositoryWorkItem.markAs(wi.id, PermanentlyFailed, None).flatMap(_ => process())
          } else {
            logger.traceWorkItem(utr, wi, "Retry poller - Call failed. reason: " + error.toString)

            ttpArrangementRepositoryWorkItem.markAs(wi.id, Failed, None).flatMap(_ => process())
          }
      }
    } catch {
      case err: Throwable =>
        logger.error("Retry poller - DES problem", err)
        throw err
    }
  }

  def process(): Future[Unit] =
    try {
      ttpArrangementRepositoryWorkItem.pullOutstanding()
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

object PollerService {

  final case class OnCompleteAction(onComplete: () => Unit)

}

