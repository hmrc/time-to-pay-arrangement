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

package uk.gov.hmrc.timetopay.arrangement.repository

import com.google.inject.Inject
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import uk.gov.hmrc.mongo.MongoUtils
import java.util.concurrent.TimeUnit
import play.api.Configuration
import uk.gov.hmrc.mongo.workitem.{WorkItem, WorkItemFields, WorkItemRepository}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.timetopay.arrangement.config.QueueConfig
import uk.gov.hmrc.timetopay.arrangement.model.TTPArrangementWorkItem
import java.time.{Clock, Instant, Duration}
import scala.concurrent.{ExecutionContext, Future}

class TTPArrangementWorkItemRepository @Inject() (configuration: Configuration,
                                                  queueConfig:   QueueConfig,
                                                  mongo:         MongoComponent,
                                                  val clock:     Clock
)(implicit ec: ExecutionContext)
  extends WorkItemRepository[TTPArrangementWorkItem](
    collectionName = "TTPArrangementsWorkItem",
    mongoComponent = mongo,
    itemFormat     = TTPArrangementWorkItem.format,
    workItemFields = WorkItemFields(
      id           = "_id",
      item         = "item",
      availableAt  = "availableAt",
      receivedAt   = "receivedAt",
      failureCount = "failureCount",
      updatedAt    = "updatedAt",
      status       = "status"
    )
  ) {

  override def now(): Instant = Instant.now()

  def inProgressRetryAfterProperty: String = queueConfig.retryAfter
  lazy val retryIntervalMillis: Long = configuration.getMillis(inProgressRetryAfterProperty)
  override lazy val inProgressRetryAfter: Duration = Duration.ofMillis(retryIntervalMillis)
  private lazy val ttlInSeconds = queueConfig.ttl.toSeconds

  override def ensureIndexes(): Future[Seq[String]] =
    MongoUtils.ensureIndexes(
      collection,
      indexes ++ additionalIndexes,
      replaceIndexes = true
    )

  def additionalIndexes: Seq[IndexModel] = Seq(
    IndexModel(
      keys         = Indexes.ascending("receivedAt"),
      indexOptions = IndexOptions().expireAfter(ttlInSeconds, TimeUnit.SECONDS)
    )
  )

  def pullOutstanding(): Future[Option[WorkItem[TTPArrangementWorkItem]]] =
    super.pullOutstanding(now().minusMillis(retryIntervalMillis.toInt), now())

  def findAll(): Future[Seq[WorkItem[TTPArrangementWorkItem]]] =
    collection.find().toFuture()
}
