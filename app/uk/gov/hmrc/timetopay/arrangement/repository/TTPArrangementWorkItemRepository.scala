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
import org.joda.time.DateTime
import play.api.Configuration
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.timetopay.arrangement.model.TTPArrangementWorkItem
import uk.gov.hmrc.workitem._


class TTPArrangementWorkItemRepository @Inject() (configuration: Configuration, reactiveMongoComponent: ReactiveMongoComponent) extends WorkItemRepository[TTPArrangementWorkItem, BSONObjectID](
  collectionName = "TTPArrangementsWorkItem",
  mongo          = reactiveMongoComponent.mongoConnector.db,
  itemFormat     = WorkItem.workItemMongoFormat[TTPArrangementWorkItem],
  config         = configuration.underlying
) {
  override def now: DateTime =
    DateTime.now
//todo change these
  override lazy val workItemFields: WorkItemFieldNames =
    new WorkItemFieldNames {
      val receivedAt = "receivedAt"
      val updatedAt = "updatedAt"
      val availableAt = "receivedAt"
      val status = "status"
      val id = "_id"
      val failureCount = "failureCount"
    }

  override val inProgressRetryAfterProperty: String =
    "queue.retryAfter"
}
