/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.timetopay.arrangement

import javax.inject.Inject

import com.google.inject.{ImplementedBy, Singleton}
import play.api.Logger
import play.api.libs.json.{Format, Json}
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.commands.DefaultWriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object TTPArrangementMongoFormats {
  import modelFormat._
  implicit val format = ReactiveMongoFormats.mongoEntity({
    Format(Json.reads[TTPArrangement], Json.writes[TTPArrangement])
  })
  val id = "_id"
}

class TTPArrangementRepository @Inject()(mongo: DB)
  extends ReactiveRepository[TTPArrangement, String]("ttparrangements",() => mongo, TTPArrangementMongoFormats.format, implicitly[Format[String]]){

  def save(ttpArrangement: TTPArrangement) : Future[Option[TTPArrangement]] = {
    Logger.debug("Saving ttparrangement record")
    insert(ttpArrangement)
      .collect {
        case DefaultWriteResult(true, 1, Seq(), None, _, None) => Logger.info(s"Arrangement record persisted ID: ${ttpArrangement.id}"); Some(ttpArrangement)
        case DefaultWriteResult(false, 1, Seq(), None, _, Some(msg)) =>
          Logger.error(s"An error occurred saving record: $msg")
          None
      }
  }

  override def indexes: Seq[Index] = Seq(
    Index(key = Seq("createdOn" -> IndexType.Ascending), name = Some("expireAtIndex"), options = BSONDocument("expireAfterSeconds" -> 2592000))
  )
}
