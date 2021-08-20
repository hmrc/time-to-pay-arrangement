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

import java.time.Clock
import java.time.Clock.systemUTC
import java.time.LocalDateTime.now
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.timetopay.arrangement.model.TTPArrangementWorkItem
import uk.gov.hmrc.timetopay.arrangement.repository.TTPArrangementWorkItemRepository
import uk.gov.hmrc.timetopay.arrangement.repository.TestDataTtp.{arrangement, auditTags}
import uk.gov.hmrc.timetopay.arrangement.support.{ITSpec, WireMockResponses}
import uk.gov.hmrc.workitem.{Failed, PermanentlyFailed, Succeeded}

class PollerServiceSpec extends ITSpec {

  private val pollerService = fakeApplication.injector.instanceOf[PollerService]
  private val arrangementWorkItemRepo = fakeApplication.injector.instanceOf[TTPArrangementWorkItemRepository]
  private val crypto = fakeApplication.injector.instanceOf[CryptoService]

  override def beforeEach(): Unit = {
    arrangementWorkItemRepo.collection.drop(false).futureValue
    ()
  }

  override def afterEach(): Unit = {
    arrangementWorkItemRepo.collection.drop(false).futureValue
    ()
  }
  private val clock: Clock = systemUTC()
  private val jodaDateTime: DateTime = DateTime.now()
  val ttpArrangementWorkItem = TTPArrangementWorkItem(now(clock), now(clock), "", crypto.encryptTtpa(arrangement), crypto.encryptAuditTags(auditTags))
  protected def numberOfQueuedNotifications: Integer = arrangementWorkItemRepo.count(Json.obj()).futureValue

  "pollerService should set it to PermanentlyFailed failed if availableUntil is passed" in {
    arrangementWorkItemRepo.pushNew(ttpArrangementWorkItem.copy(availableUntil = now(clock).minusYears(1)), jodaDateTime).futureValue
    pollerService.process().futureValue
    eventually {
      arrangementWorkItemRepo.findAll().futureValue.head.status shouldBe PermanentlyFailed
    }
  }

  "pollerService should set it to failed if des call fails " in {
    arrangementWorkItemRepo.pushNew(ttpArrangementWorkItem.copy(availableUntil = now(clock).plusMinutes(10)), jodaDateTime).futureValue
    pollerService.process().futureValue
    eventually {
      arrangementWorkItemRepo.findAll().futureValue.head.status shouldBe Failed
    }
  }

  "pollerService should set it to complete if des call is successful and remove from repo " in {
    WireMockResponses.desArrangementApiSucccess(arrangement.taxpayer.selfAssessment.utr)
    arrangementWorkItemRepo.pushNew(ttpArrangementWorkItem.copy(availableUntil = now(clock).plusMinutes(10)), jodaDateTime).futureValue
    pollerService.process().futureValue
    eventually {
      arrangementWorkItemRepo.findAll().futureValue.size shouldBe 0
    }
  }
}
