package uk.gov.hmrc.timetopay.arrangement

import play.api.Logger
import play.api.libs.json.{Format, Json}
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


class TTPArrangementRepository(implicit mongo: () => DB) extends ReactiveRepository[TTPArrangement, String]("ttparrangements", mongo, TTPArrangementMongoFormats.format, implicitly[Format[String]]) {

  def save(ttpArrangement: TTPArrangement) : Future[Option[TTPArrangement]] = {
    Logger.debug("Saving ttparrangement record")
    insert(ttpArrangement)
      .collect {
        case DefaultWriteResult(true, 1, Seq(), None, _, None) => Logger.info("arrangement record persisted"); Some(ttpArrangement)
        case DefaultWriteResult(false, 1, Seq(), None, _, Some(msg)) =>
          Logger.error(s"An error occurred saving record: $msg")
          None
      }
  }

  override def indexes: Seq[Index] = Seq(
    Index(key = Seq("createdOn" -> IndexType.Ascending), name = Some("expireAtIndex"), options = BSONDocument("expireAfterSeconds" -> 2592000))
  )
}
