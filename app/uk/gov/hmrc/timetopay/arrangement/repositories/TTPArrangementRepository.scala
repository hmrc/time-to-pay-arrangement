package uk.gov.hmrc.timetopay.arrangement.repositories

import org.joda.time.{DateTimeZone, DateTime}
import play.api.Logger
import play.api.libs.json.{Json, Format}
import reactivemongo.api.DB
import reactivemongo.api.commands.DefaultWriteResult
import reactivemongo.bson.BSONDocument
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.timetopay.arrangement.models.TTPArrangement
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object TTPArrangementMongoFormats {
  implicit val format = ReactiveMongoFormats.mongoEntity({
    Format(Json.reads[TTPArrangement], Json.writes[TTPArrangement])
  })
}


class TTPArrangementRepository(implicit mongo: () => DB) extends ReactiveRepository[TTPArrangement, String]("ttparrangements", mongo, TTPArrangementMongoFormats.format, implicitly[Format[String]]) {

  def save(ttpArrangement: TTPArrangement) : Future[Option[TTPArrangement]] = {
    Logger.debug("Saving ttparrangement record")
    insert(ttpArrangement)
      .collect {
        case DefaultWriteResult(true, 1, Seq(), None, _, None) => Logger.debug("arrangement record persisted"); Some(ttpArrangement)
        case DefaultWriteResult(false, 1, Seq(), None, _, Some(msg)) => throw new Exception(s"An error occurred saving record: $msg")
      }
  }

  def get(id: String): Future[Option[TTPArrangement]] = {
    Logger.info(s"Getting TTP arrangement for $id")
    collection.find(BSONDocument())
      .sort(Json.obj("identifier" -> id))
      .one[TTPArrangement]
  }

}
