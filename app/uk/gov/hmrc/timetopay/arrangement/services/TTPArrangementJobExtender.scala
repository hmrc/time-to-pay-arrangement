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

import org.mongodb.scala.model.Filters.equal
import play.api.{Configuration, Logging}
import uk.gov.hmrc.timetopay.arrangement.repository.TTPArrangementWorkItemRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

@Singleton
class TTPArrangementJobExtender @Inject() (
    ttpArrangementWorkItemRepo: TTPArrangementWorkItemRepository,
    config:                     Configuration
)(implicit ec: ExecutionContext) extends Logging {

  private val enabled = config.get[Boolean]("queue.available-for-extender.enabled")

  private val extendByMinutes: Long = config.get[FiniteDuration]("queue.available-for-extender.extend-by").toMinutes

  def extend(): Future[Unit] = {
    if (enabled) {
      logger.info("TTPArrangementJobExtender enabled - getting all work items")
      for {
        currentItems <- ttpArrangementWorkItemRepo.findAll()
        _ <- {
          logger.info(s"About to adjust availableFor value for ${currentItems.size.toString} items")
          Future.sequence(currentItems.map(workItem =>
            ttpArrangementWorkItemRepo.collection.replaceOne(
              equal("_id", workItem.id),
              workItem.copy(item = workItem.item.copy(
                availableUntil = workItem.item.availableUntil.plusMinutes(extendByMinutes))
              )
            ).toFuture()
          ))
        }
      } yield {
        logger.info(s"availableFor value successfully adjusted for ${currentItems.size.toString} items")
        ()
      }
    } else {
      logger.info("TTPArrangementJobExtender not enabled - not doing anything")
      Future.successful(())
    }
  }

  extend()

}
