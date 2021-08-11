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

import java.time.{Clock, LocalDateTime}

import akka.actor.ActorSystem
import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.timetopay.arrangement.config.QueueConfig
import uk.gov.hmrc.timetopay.arrangement.connectors.{DesArrangementApiServiceConnector, SubmissionError, SubmissionSuccess}
import uk.gov.hmrc.timetopay.arrangement.model.{DesSubmissionRequest, TTPArrangementWorkItem}
import uk.gov.hmrc.timetopay.arrangement.repository.TTPArrangementWorkItemRepository
import uk.gov.hmrc.workitem.{Failed, PermanentlyFailed, WorkItem}
import uk.gov.hmrc.play.scheduling.ExclusiveScheduledJob
import scala.concurrent.{ExecutionContext, Future}

class PollerService @Inject() (
    actorSystem:                       ActorSystem,
    desArrangementApiServiceConnector: DesArrangementApiServiceConnector,
    ttpArrangementRepositoryWorkItem:  TTPArrangementWorkItemRepository,
    queueConfig:                       QueueConfig,
    val clock:                         Clock)(
    implicit
    ec: ExecutionContext
) extends ExclusiveScheduledJob {

  private val logger: Logger = Logger(this.getClass.getSimpleName)
  val initialDelay = queueConfig.initialDelay
  val interval = queueConfig.interval

  override def executeInMutex(implicit ec: ExecutionContext): Future[Result] =
    process().map(_ => Result(""))

  override def name: String = "TTP Arrangement Poller"
  callExecutor(name)
  def callExecutor(name: String)(implicit ec: ExecutionContext) = {
    actorSystem.scheduler.scheduleWithFixedDelay(initialDelay, interval)(() => {
      logger.info("Running poller " + name)
      process()
      ()
    })
  }

  def isAvailable(workItem: TTPArrangementWorkItem): Boolean = {
    val time = LocalDateTime.now(clock)
    time.isBefore(workItem.availableUntil)
  }

  def tryDesCallAgain(wi: WorkItem[TTPArrangementWorkItem]) = {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val arrangment = wi.item.ttpArrangement
    val utr = arrangment.taxpayer.selfAssessment.utr
    val desSubmissionRequest: DesSubmissionRequest = arrangment.desArrangement
      .getOrElse(throw new RuntimeException("Saved ttp in work item repo had no desArrangement to send " + wi.toString))

    desArrangementApiServiceConnector.submitArrangement(utr, desSubmissionRequest).map {
      case Right(_) =>
        ttpArrangementRepositoryWorkItem.complete(wi.id).map(_ => process())
        ()
      case Left(error) =>
        logger.warn("Call failed for " + wi.toString + " reason " + error.toString)
        ttpArrangementRepositoryWorkItem.markAs(wi.id, Failed, None).map(_ => process())
        ()
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def process(): Future[Unit] =
    ttpArrangementRepositoryWorkItem.pullOutstanding
      .flatMap {
        case None =>
          Future.successful(())
        case Some(wi) =>
          if (isAvailable(wi.item)) {
            logger.info("Retrying call to des api for " + wi.toString)
            tryDesCallAgain(wi)
          } else {
            logger.error("Call failed and will not be tried again for " + wi.toString)
            ttpArrangementRepositoryWorkItem.markAs(wi.id, PermanentlyFailed, None).flatMap(_ => process())
          }
      }

}

