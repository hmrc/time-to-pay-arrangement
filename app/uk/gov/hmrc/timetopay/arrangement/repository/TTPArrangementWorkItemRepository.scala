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

import com.google.inject.Inject
import org.joda.time.{DateTime, Duration}
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.timetopay.arrangement.config.QueueConfig
import uk.gov.hmrc.timetopay.arrangement.model.TTPArrangementWorkItem
import uk.gov.hmrc.workitem._

import java.time.{Clock, ZoneId}
import scala.concurrent.{ExecutionContext, Future}

class TTPArrangementWorkItemRepository @Inject() (configuration:          Configuration,
                                                  queueConfig:            QueueConfig,
                                                  reactiveMongoComponent: ReactiveMongoComponent,
                                                  val clock:              Clock,
                                                 )
  extends WorkItemRepository[TTPArrangementWorkItem, BSONObjectID](
    collectionName = "TTPArrangementsWorkItem",
    mongo          = reactiveMongoComponent.mongoConnector.db,
    itemFormat     = WorkItem.workItemMongoFormat[TTPArrangementWorkItem],
    config         = configuration.underlying
  ) {

  override def now: DateTime = new DateTime(clock.millis())

  override def inProgressRetryAfterProperty: String = queueConfig.retryAfter
  lazy val retryIntervalMillis: Long = configuration.getMillis(inProgressRetryAfterProperty)
  override lazy val inProgressRetryAfter: Duration = Duration.millis(retryIntervalMillis)
  private lazy val ttlInSeconds = queueConfig.ttl.toSeconds

  override lazy val workItemFields: WorkItemFieldNames =
    new WorkItemFieldNames {
      val receivedAt = "receivedAt"
      val updatedAt = "updatedAt"
      val availableAt = "receivedAt"
      val status = "status"
      val id = "_id"
      val failureCount = "failureCount"
    }

  override def indexes: Seq[Index] = super.indexes ++ Seq(
    Index(
      key     = Seq(workItemFields.receivedAt -> IndexType.Ascending),
      name    = Some("receivedAtTime"),
      options = BSONDocument("expireAfterSeconds" -> ttlInSeconds)
    ))

  def pullOutstanding(implicit ec: ExecutionContext): Future[Option[WorkItem[TTPArrangementWorkItem]]] =
    super.pullOutstanding(now.minusMillis(retryIntervalMillis.toInt), now)

  def complete(id: BSONObjectID)(implicit ec: ExecutionContext): Future[Boolean] = {
    val selector = JsObject(
      Seq("_id" -> Json.toJson(id)(ReactiveMongoFormats.objectIdFormats), "status" -> Json.toJson(InProgress: ProcessingStatus)))
    collection.delete().one(selector).map(_.n > 0)
  }

  def failed(id: BSONObjectID)(implicit ec: ExecutionContext): Future[Boolean] = {
    markAs(id, Failed, Some(now.plusMillis(retryIntervalMillis.toInt)))
  }

}
