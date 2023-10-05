
package tasks

import org.mongodb.scala.MongoDatabase
import play.api.Logging
import play.api.inject._
import uk.gov.hmrc.mongo.MongoComponent

import javax.inject.{Inject, Singleton}

class TasksModule extends SimpleModule(bind[DropCollectionsTask].toSelf.eagerly())

@Singleton
class DropCollectionsTask @Inject() (mongoComponent: MongoComponent) extends Logging {
  logger.info("**************** Start cleanup task: drop alerts_received mongodb collections...")

  private val collectionsToDrop = List("ttparrangements", "ttparrangements-new-mongo")
  val database: MongoDatabase = mongoComponent.client.getDatabase("time-to-pay-arrangement")
  collectionsToDrop.map(collection => database.getCollection(collection).drop().toFuture())
  .foreach { _ => logger.info("**************** cleanup done.") }
}

