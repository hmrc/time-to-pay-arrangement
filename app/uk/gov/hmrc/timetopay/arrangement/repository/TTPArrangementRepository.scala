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

import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.ReadPreference
import reactivemongo.api.commands.DefaultWriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.timetopay.arrangement.TTPArrangement

import javax.inject.Inject

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
  val id = "_id"
}

class TTPArrangementRepository @Inject() (reactiveMongoComponent: ReactiveMongoComponent)
  extends ReactiveRepository[TTPArrangement, String]("ttparrangements", reactiveMongoComponent.mongoConnector.db, TTPArrangementMongoFormats.format, implicitly[Format[String]]) {

  def findByIdLocal(id: String, readPreference: ReadPreference = ReadPreference.primaryPreferred)(implicit ec: ExecutionContext): Future[Option[JsValue]] = {

    collection.find(_id(id), None)(new OWrites[JsObject] {
      def writes(o: JsObject): JsObject = o
    }, new OWrites[JsObject] {
      def writes(o: JsObject): JsObject = o
    }).one[JsValue](readPreference)
  }

  def doInsert(ttpArrangement: TTPArrangement)(implicit ec: ExecutionContext): Future[Option[TTPArrangement]] = {
    logger.debug("Saving ttparrangement record")
    insert(ttpArrangement)
      .collect {
        case DefaultWriteResult(true, 1, Seq(), None, _, None) =>
          logger.info(s"Arrangement record persisted ID: ${ttpArrangement.id}")
          Some(ttpArrangement)
        case DefaultWriteResult(false, 1, Seq(), None, _, Some(msg)) =>
          logger.error(s"An error occurred saving record: $msg")
          None
      }
  }

  override def indexes: Seq[Index] = Seq(
    Index(key     = Seq("createdOn" -> IndexType.Ascending), name = Some("expireAtIndex"), options = BSONDocument("expireAfterSeconds" -> 2592000)))
}
