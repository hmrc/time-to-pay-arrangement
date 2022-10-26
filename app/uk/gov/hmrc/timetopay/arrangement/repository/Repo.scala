
package uk.gov.hmrc.timetopay.arrangement.repository

import org.mongodb.scala.model.{Filters, IndexModel, ReplaceOptions}
import org.mongodb.scala.result.InsertOneResult
import play.api.libs.json._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.{ExecutionContext, Future}

trait Id {
  def value: String
}

trait HasId[ID <: Id] {
  def _id: ID
  def id: ID = _id
}


abstract class Repo[ID <: Id, A <: HasId[ID]](
                                      collectionName: String,
                                      mongoComponent: MongoComponent,
                                      indexes:        Seq[IndexModel],
                                      replaceIndexes: Boolean         = false
                                    )(implicit manifest: Manifest[A],
                                      domainFormat:     OFormat[A],
                                      executionContext: ExecutionContext,
//                                      id:               Id[ID],
//                                      idExtractor:      IdExtractor[A, ID]
                                    )
  extends PlayMongoRepository[A](
    mongoComponent = mongoComponent,
    collectionName = collectionName,
    domainFormat   = domainFormat,
    indexes        = indexes,
    replaceIndexes = replaceIndexes
  ) {

  /**
   * Update or Insert (upsert) element `a` identified by `id`
   */
  def upsert(a: A): Future[Unit] = {
    collection
      .replaceOne(
        filter = Filters.eq("_id", a.id.value),
        replacement = a,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => ())
  }

  def insertOne(a: A): Future[Option[InsertOneResult]] = {
    collection
      .insertOne(a)
      .toFutureOption()
  }

  def findById(id: ID): Future[Option[A]] = {
    collection
      .find(
        filter = Filters.eq("_id", id.value)
      )
      .headOption()
  }
}