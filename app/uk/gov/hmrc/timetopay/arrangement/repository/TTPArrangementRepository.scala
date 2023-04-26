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

import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes}
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import javax.inject.Inject
import uk.gov.hmrc.timetopay.arrangement.model.AnonymousTTPArrangement
import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}

class TTPArrangementRepository @Inject() (
    mongo:  MongoComponent,
    config: ServicesConfig
)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[AnonymousTTPArrangement](
    mongoComponent = mongo,
    collectionName = "ttparrangements-new-mongo",
    domainFormat   = AnonymousTTPArrangement.format,
    indexes        = TTPArrangementRepository.indexes(config.getDuration("TTPArrangement.ttl").toSeconds),
    replaceIndexes = true
  ) {

  def findById(id: String): Future[Option[AnonymousTTPArrangement]] = {
    collection
      .find(
        filter = Filters.eq("_id", id)
      )
      .headOption()
  }

  def doInsert(ttpArrangement: AnonymousTTPArrangement): Future[Option[AnonymousTTPArrangement]] = {
    collection
      .insertOne(ttpArrangement)
      .toFutureOption()
      .map {
        case Some(_) => Some(ttpArrangement)
        case None    => None
      }
  }
}

object TTPArrangementRepository {
  def indexes(cacheTtlInSeconds: Long): Seq[IndexModel] = Seq (
    IndexModel(
      keys         = Indexes.ascending("createdOn"),
      indexOptions = IndexOptions().expireAfter(cacheTtlInSeconds, TimeUnit.SECONDS)
    )
  )
}
