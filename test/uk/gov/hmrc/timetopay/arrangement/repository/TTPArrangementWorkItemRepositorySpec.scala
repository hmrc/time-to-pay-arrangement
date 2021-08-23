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

package uk.gov.hmrc.timetopay.arrangement.repository

import java.time.Clock
import java.time.Clock.systemUTC
import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.hmrc.timetopay.arrangement.model.TTPArrangementWorkItem
import uk.gov.hmrc.timetopay.arrangement.support.{ITSpec, MongoSupport}
import uk.gov.hmrc.timetopay.arrangement.repository.TestDataTtp.{arrangement, auditTags}

import java.time.LocalDateTime.now
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.timetopay.arrangement.services.CryptoService
import uk.gov.hmrc.workitem.{Cancelled, Deferred, Duplicate, Failed, Ignored, InProgress, PermanentlyFailed, Succeeded, WorkItem}

class TTPArrangementWorkItemRepositorySpec extends ITSpec {

  private lazy val repo = fakeApplication.injector.instanceOf[TTPArrangementWorkItemRepository]
  private val crypto = fakeApplication.injector.instanceOf[CryptoService]
  private val jodaDateTime: DateTime = DateTime.now()

  private val clock: Clock = systemUTC()

  val ttpArrangementWorkItem = TTPArrangementWorkItem(now(clock), now(clock), "", crypto.encryptTtpa(arrangement), crypto.encryptAuditTags(auditTags))
  "Count should be 0 with empty repo" in {
    collectionSize shouldBe 0
  }

  "ensure indexes are created" in {
    repo.drop.futureValue
    repo.ensureIndexes.futureValue
    repo.collection.indexesManager.list().futureValue.size shouldBe 5
  }

  "be able to push a new request and reload a request" in {
    val workItem = repo.pushNew(ttpArrangementWorkItem, jodaDateTime).futureValue
    val found = repo.findById(workItem.id).futureValue

    found match {
      case Some(x) =>
        x.status shouldBe uk.gov.hmrc.workitem.ToDo
        crypto.decryptTtpa(x.item.ttpArrangement) shouldBe Some(arrangement)
      case None => "failed" shouldBe "to find a value"
    }
  }

  "be able to pull a request" in {
    val _ = repo.pushNew(ttpArrangementWorkItem, jodaDateTime).futureValue
    val outstanding: Option[WorkItem[TTPArrangementWorkItem]] = repo.pullOutstanding.futureValue
    outstanding match {
      case Some(x) =>
        x.status shouldBe uk.gov.hmrc.workitem.InProgress
        crypto.decryptTtpa(x.item.ttpArrangement) shouldBe Some(arrangement)
      case None => "failed" shouldBe "to find a value"
    }

    val outstanding2: Option[WorkItem[TTPArrangementWorkItem]] = repo.pullOutstanding.futureValue
    outstanding2 match {
      case Some(_) =>
        "found" shouldBe "a value when we should not"
      case None =>
    }
  }
  Seq(Failed, InProgress, Duplicate, Cancelled, Ignored, Deferred, PermanentlyFailed, Succeeded).foreach(status =>
    s"Pull a request with a status of ${status.toString} should not find anything if we have not waited" in {
      val workItem = repo.pushNew(ttpArrangementWorkItem, jodaDateTime).futureValue
      repo.markAs(workItem.id, status).futureValue should be(true)
      val outstanding: Option[WorkItem[TTPArrangementWorkItem]] = repo.pullOutstanding.futureValue
      outstanding match {
        case Some(_) =>
          "found" shouldBe "a value when we should not"
        case None =>
      }

    }
  )

  Seq(Duplicate, Cancelled, Ignored, Deferred, PermanentlyFailed, Succeeded).foreach(status =>
    s"Pull a request with a status of ${status.toString} should not find anything, we have waited" in {
      val workItem = repo.pushNew(ttpArrangementWorkItem, jodaDateTime).futureValue
      repo.markAs(workItem.id, status).futureValue should be(true)

      eventually {
        val outstanding: Option[WorkItem[TTPArrangementWorkItem]] = repo.pullOutstanding.futureValue
        outstanding match {
          case Some(_) => "found" shouldBe "a value when we should not"
          case None    =>
        }
      }
    }
  )

  "complete and delete an in progress request" in {
    val workItem = repo.pushNew(ttpArrangementWorkItem, jodaDateTime).futureValue
    repo.markAs(workItem.id, InProgress).futureValue should be(true)
    repo.complete(workItem.id).futureValue should be(true)
    repo.findById(workItem.id).futureValue shouldBe None
  }

  "cannot complete a request if it is not in progress" in {
    val workItem = repo.pushNew(ttpArrangementWorkItem, jodaDateTime).futureValue
    repo.complete(workItem.id).futureValue should be(false)
    val workItemUpdated = repo.findById(workItem.id).futureValue
    workItemUpdated match {
      case Some(x) => x.status shouldBe uk.gov.hmrc.workitem.ToDo
      case None    => "failed" shouldBe "to find a workitem"
    }
  }

  "Cannot complete a request if it cannot be found" in {
    repo.complete(BSONObjectID.generate).futureValue should be(false)
  }

  private def collectionSize: Int = {
    repo.count(Json.obj()).futureValue
  }
}
