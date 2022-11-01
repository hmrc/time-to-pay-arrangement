/*
 * Copyright 2022 HM Revenue & Customs
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
import org.mongodb.scala.ReadPreference
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import uk.gov.hmrc.timetopay.arrangement.model.{TTPArrangementAnon, TTPArrangement}

import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}
//The below is needed !

object TTPArrangementMongoFormats {
  implicit val customWriterTTPArrangementMongo: Writes[TTPArrangement] = {
    val pathToPersonalInfo = Seq(
      (__ \ "taxpayer" \ "customerName").json.prune,
      (__ \ "taxpayer" \ "addresses").json.prune,
      (__ \ "taxpayer" \ "selfAssessment" \ "communicationPreferences").json.prune,
      (__ \ "taxpayer" \ "selfAssessment" \ "debits").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "customerName").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "salutation").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "addressLine1").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "addressLine2").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "addressLine3").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "addressLine4").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "addressLine5").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "postCode").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "totalAll").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "clmIndicateInt").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "clmPymtString").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "officeName1").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "officeName2").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "officePostcode").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "officePhone").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "officeFax").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "officeOpeningHours").json.prune,
      (__ \ "desArrangement" \ "letterAndControl" \ "template").json.prune)

      def pruneAll(jspaths: Seq[Reads[JsObject]], jsObject: JsObject): JsObject = {
        jspaths.foldLeft(jsObject) { (acc, path) => acc.transform(path).get }
      }

    Json.writes[TTPArrangement].transform { json: JsValue =>
      json match {
        case obj: JsObject =>
          pruneAll(pathToPersonalInfo, obj)
        case other => other
      }
    }
  }
  implicit val format: Format[TTPArrangement] = ReactiveMongoFormats.mongoEntity({
    Format(Json.reads[TTPArrangement], customWriterTTPArrangementMongo)
  })

  implicit val newFormat: Format[TTPArrangement] = Format[TTPArrangement](Json.reads[TTPArrangement], customWriterTTPArrangementMongo)

  val id = "_id"

}

class TTPArrangementRepository @Inject() (
    mongo:  MongoComponent,
    config: ServicesConfig
)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[TTPArrangementAnon](
    mongoComponent = mongo,
    collectionName = "ttparrangements-new-mongo",
    domainFormat   = TTPArrangementAnon.format,
    //    extraCodecs = Seq(Codecs.playFormatCodec(Json.format[JsValue])),
    indexes        = TTPArrangementRepository.indexes(config.getDuration("TTPArrangement.ttl").toSeconds),
    replaceIndexes = true
  ) {

  def findByIdLocal(
      id:             String,
      readPreference: ReadPreference = ReadPreference.primaryPreferred
  ): Future[Option[TTPArrangementAnon]] = {
    collection.withReadPreference(readPreference)
      .find(
        filter = Filters.eq("_id", id)
      )
      .headOption()
  }

  def doInsert(ttpArrangement: TTPArrangementAnon): Future[Option[TTPArrangementAnon]] = {
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
      keys         = Indexes.ascending("expireAtIndex"),
      indexOptions = IndexOptions().expireAfter(cacheTtlInSeconds, TimeUnit.SECONDS)
    )
  )
}
