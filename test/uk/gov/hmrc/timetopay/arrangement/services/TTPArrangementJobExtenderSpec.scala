/*
 * Copyright 2025 HM Revenue & Customs
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

import org.scalatest.exceptions.TestFailedDueToTimeoutException
import play.api.Configuration
import play.api.test.Helpers._
import uk.gov.hmrc.mongo.workitem.{ProcessingStatus, WorkItem}
import uk.gov.hmrc.timetopay.arrangement.model.TTPArrangementWorkItem
import uk.gov.hmrc.timetopay.arrangement.repository.TTPArrangementWorkItemRepository
import uk.gov.hmrc.timetopay.arrangement.support.ITSpec

import java.time.{Instant, LocalDateTime}
import java.time.temporal.ChronoField.NANO_OF_SECOND

class TTPArrangementJobExtenderSpec extends ITSpec {

  lazy val workItemRepo = app.injector.instanceOf[TTPArrangementWorkItemRepository]

  override def beforeAll(): Unit = {
    super.beforeAll()
    await(workItemRepo.collection.drop().toFuture())
  }

  override def afterEach(): Unit = {
    super.afterEach()
    await(workItemRepo.collection.drop().toFuture())
  }

  class TestSetup(configuration: Configuration) {
    // work items returned by pushNew have sub microsecond precision for
    // Instants, work items from mongo don't not have that level of precision.
    // Remove sub microsecond precision to allow comparison
    def adjustTimePrecision[A](workItem: WorkItem[A]) = {
        def adjustInstant(instant: Instant) = {
          val withMicrosecondPrecision = instant.getNano - (instant.getNano % 1000000)
          instant.`with`(NANO_OF_SECOND, withMicrosecondPrecision)
        }

      workItem.copy(
        receivedAt  = adjustInstant(workItem.receivedAt),
        updatedAt   = adjustInstant(workItem.updatedAt),
        availableAt = adjustInstant(workItem.availableAt)
      )
    }

    val originalAvailableUntil = LocalDateTime.of(2000, 5, 20, 2, 0)

    val originalWorkItem = TTPArrangementWorkItem(
      LocalDateTime.now(),
      originalAvailableUntil,
      "ref",
      "arrangement",
      "auditTags"
    )

    // insert a new work item
    val newWorkItem = adjustTimePrecision(await(workItemRepo.pushNew(originalWorkItem)))

    eventually{
      await(workItemRepo.findById(newWorkItem.id)) shouldBe Some(newWorkItem)
    }

    // update the work item with a new status
    val workItem = newWorkItem.copy(
      status       = ProcessingStatus.PermanentlyFailed,
      failureCount = 5
    )

    // mark it as permanently failed
    await(workItemRepo.collection.replaceOne(
      org.mongodb.scala.model.Filters.equal("_id", newWorkItem.id),
      workItem
    ).toFuture())

    eventually{
      await(workItemRepo.findById(workItem.id)) shouldBe Some(workItem)
    }

    val extender = new TTPArrangementJobExtender(workItemRepo, configuration)
  }

  "TTPArrangementJobExtender must" - {

    "extend the availableUntil value for items in " +
      "the TTP arrangement work item repo if enabled" in new TestSetup(
        Configuration(
          "queue.available-for-extender.enabled" -> true,
          "queue.available-for-extender.extend-by" -> "6 days"
        )
      ) {
        eventually{
          await(workItemRepo.findById(workItem.id)) shouldBe Some(
            workItem.copy(
              item         = originalWorkItem.copy(availableUntil = LocalDateTime.of(2000, 5, 26, 2, 0)),
              status       = ProcessingStatus.ToDo,
              failureCount = 0
            )
          )
        }
      }

    "reduce the availableUntil value for items in " +
      "the TTP arrangement work item repo if enabled" in new TestSetup(
        Configuration(
          "queue.available-for-extender.enabled" -> true,
          "queue.available-for-extender.extend-by" -> "-25h"
        )
      ) {
        eventually{
          await(workItemRepo.findById(workItem.id)) shouldBe Some(
            workItem.copy(
              item         = originalWorkItem.copy(availableUntil = LocalDateTime.of(2000, 5, 19, 1, 0)),
              status       = ProcessingStatus.ToDo,
              failureCount = 0
            )
          )
        }
      }

    "not change any work items if the job is not enabled" in new TestSetup(
      Configuration(
        "queue.available-for-extender.enabled" -> false,
        "queue.available-for-extender.extend-by" -> "-25h"
      )
    ) {
      a[TestFailedDueToTimeoutException] shouldBe thrownBy{
        eventually{
          await(workItemRepo.findById(workItem.id)) shouldBe Some(
            workItem.copy(
              item         = originalWorkItem.copy(availableUntil = LocalDateTime.of(2000, 5, 19, 1, 0)),
              status       = ProcessingStatus.ToDo,
              failureCount = 0
            )
          )
        }
      }
    }

  }

}
