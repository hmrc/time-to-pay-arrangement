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

import com.github.tomakehurst.wiremock.client.WireMock
import org.scalatest.exceptions.TestFailedDueToTimeoutException

import java.time.{Clock, Instant}
import java.time.Clock.systemUTC
import java.time.LocalDateTime.now
import uk.gov.hmrc.timetopay.arrangement.model.TTPArrangementWorkItem
import uk.gov.hmrc.timetopay.arrangement.repository.TTPArrangementWorkItemRepository
import uk.gov.hmrc.timetopay.arrangement.repository.TestDataTtp.{arrangement, auditTags}
import uk.gov.hmrc.timetopay.arrangement.support.{ITSpec, WireMockResponses}
import uk.gov.hmrc.mongo.workitem.ProcessingStatus.{Failed, PermanentlyFailed}

import scala.concurrent.duration._

class PollerServiceSpec extends ITSpec {

  private val pollerService = app.injector.instanceOf[PollerService]
  private val arrangementWorkItemRepo = app.injector.instanceOf[TTPArrangementWorkItemRepository]
  private val crypto = app.injector.instanceOf[CryptoService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    arrangementWorkItemRepo.collection.drop().toFuture().futureValue
    ()
  }

  override def afterEach(): Unit = {
    super.afterEach()
    arrangementWorkItemRepo.collection.drop().toFuture().futureValue
    ()
  }

  private val clock: Clock = systemUTC()

  private val javaInstantNow: Instant = Instant.now()

  val ttpArrangementWorkItem =
    TTPArrangementWorkItem(
      now(clock),
      now(clock),
      "",
      crypto.encryptTtpa(arrangement),
      crypto.encryptAuditTags(auditTags)
    )

  protected def numberOfQueuedNotifications: Integer =
    arrangementWorkItemRepo.collection.countDocuments().toFuture().futureValue.toInt

  "PollerService when" - {

    "process() is called should" - {

      "set it the job status to PermanentlyFailed failed if availableUntil is passed" in {
        arrangementWorkItemRepo.pushNew(ttpArrangementWorkItem.copy(availableUntil = now(clock).minusYears(1)), javaInstantNow).futureValue
        pollerService.process().futureValue
        eventually {
          arrangementWorkItemRepo.collection.find().head().futureValue.status shouldBe PermanentlyFailed
        }
      }

      "set the job status to Failed if des call fails " in {
        arrangementWorkItemRepo.pushNew(ttpArrangementWorkItem.copy(availableUntil = now(clock).plusMinutes(10)), javaInstantNow).futureValue
        pollerService.process().futureValue
        eventually {
          arrangementWorkItemRepo.collection.find().head().futureValue.status shouldBe Failed
        }
      }

      "set the job status to complete if des call is successful and remove from repo " in {
        WireMockResponses.desArrangementApiSucccess(arrangement.taxpayer.selfAssessment.utr)
        arrangementWorkItemRepo.pushNew(ttpArrangementWorkItem.copy(availableUntil = now(clock).plusMinutes(10)), javaInstantNow).futureValue
        pollerService.process().futureValue
        eventually {
          arrangementWorkItemRepo.collection.countDocuments().toFuture().futureValue.toInt shouldBe 0
        }
      }
    }

    "handling it's own scheduled calls should" - {

      "reattempt calls to DES at the configured times" in {
        WireMockResponses.desArrangementApiSucccess(arrangement.taxpayer.selfAssessment.utr)

        // put some jobs on the queue
        arrangementWorkItemRepo.pushNew(ttpArrangementWorkItem.copy(availableUntil = now(clock).plusDays(1)), javaInstantNow).futureValue
        arrangementWorkItemRepo.pushNew(ttpArrangementWorkItem.copy(availableUntil = now(clock).plusDays(2)), javaInstantNow).futureValue

        // bring the time to just before the initial scheduled run and check nothing happens
        virtualTime.advance(14.seconds - 1.millisecond)

        a[TestFailedDueToTimeoutException] shouldBe thrownBy(eventually {
          WireMockResponses.ensureDesArrangementcalled(2, arrangement.taxpayer.selfAssessment.utr)
        })

        // bring the time to the initial scheduled run and check queue is handled
        virtualTime.advance(1.millisecond)

        eventually {
          WireMockResponses.ensureDesArrangementcalled(2, arrangement.taxpayer.selfAssessment.utr)
          arrangementWorkItemRepo.collection.countDocuments().toFuture().futureValue.toInt shouldBe 0
          pollerServiceOnCompleteListener.hasCompleted() shouldBe true
        }

        pollerServiceOnCompleteListener.reset()
        WireMock.resetAllRequests()

        // add something else to the next batch
        arrangementWorkItemRepo.pushNew(ttpArrangementWorkItem.copy(availableUntil = now(clock).plusDays(3)), javaInstantNow).futureValue

        // bring the time to just before the next scheduled run and check nothing happens
        virtualTime.advance(2.minutes - 1.millisecond)

        a[TestFailedDueToTimeoutException] shouldBe thrownBy(eventually {
          WireMockResponses.ensureDesArrangementcalled(1, arrangement.taxpayer.selfAssessment.utr)
        })

        // bring the time to the next scheduled run and check queue is handled
        virtualTime.advance(1.millisecond)

        eventually {
          WireMockResponses.ensureDesArrangementcalled(1, arrangement.taxpayer.selfAssessment.utr)
          arrangementWorkItemRepo.collection.countDocuments().toFuture().futureValue.toInt shouldBe 0
          pollerServiceOnCompleteListener.hasCompleted() shouldBe true
        }

        pollerServiceOnCompleteListener.reset()
      }

    }

  }
}
