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
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendHeaderCarrierProvider
import uk.gov.hmrc.timetopay.arrangement.config.QueueConfig
import uk.gov.hmrc.timetopay.arrangement.model.TTPArrangementWorkItem
import uk.gov.hmrc.timetopay.arrangement.repository.TTPArrangementWorkItemRepository
import uk.gov.hmrc.workitem.{Failed, PermanentlyFailed, WorkItem}

import scala.concurrent.{ExecutionContext, Future}

class PollerService @Inject() (
    actorSystem:                      ActorSystem,
    desArrangementApiService:         TTPArrangementService,
    ttpArrangementRepositoryWorkItem: TTPArrangementWorkItemRepository,
    queueConfig:                      QueueConfig,
    val clock:                        Clock)(
    implicit
    ec: ExecutionContext
) {

  private val logger: Logger = Logger(this.getClass.getSimpleName)
  val initialDelay = queueConfig.initialDelay
  val interval = queueConfig.interval
  //todo hack up tests tomorrow
  def run() = {
    actorSystem.scheduler.scheduleWithFixedDelay(initialDelay, interval)(() => {
      logger.info("Running poller ")
      process()
      ()
    })
  }
  def isAvailable(workItem: TTPArrangementWorkItem): Boolean = {
    val time = LocalDateTime.now(clock)
    time.isBefore(workItem.availableUntil)
  }
  //todo perhaps add some limit in I dont think there will be too many failed arrangments in Prod?
  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def process(): Future[Unit] =
    ttpArrangementRepositoryWorkItem.pullOutstanding
      .flatMap {
        case None =>
          Future.successful(())
        case Some(wi) =>
          logger.info("Retrying call to des api for " + wi.toString)
          if (isAvailable(wi.item)) {
            //todo change check that this works!!
            implicit val hc: HeaderCarrier = HeaderCarrier()
            desArrangementApiService.submit(wi.item.ttpArrangement).flatMap {
              case _ =>
                ttpArrangementRepositoryWorkItem.complete(wi.id)
                process()

            }.recover {
              case x@DesApiException(_, _) =>
                logger.warn("Call failed for " + wi.toString + " reason " + x)
                ttpArrangementRepositoryWorkItem.markAs(wi.id, Failed, None)
                process()
                ()
            }
          } else {
            logger.error("Call failed and will not be tried again for " + wi.toString)
            ttpArrangementRepositoryWorkItem.markAs(wi.id, PermanentlyFailed, None)
            process()
          }

      }
}

