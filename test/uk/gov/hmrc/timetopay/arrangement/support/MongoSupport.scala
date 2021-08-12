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

package uk.gov.hmrc.timetopay.arrangement.support

import org.scalactic.source.Position
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{AppendedClues, BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import play.api.Logging
import play.api.libs.json.Json
import reactivemongo.play.json.ImplicitBSONHandlers
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.mongo.MongoSpecSupport

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

trait MongoSupport extends MongoSpecSupport with BeforeAndAfterAll with BeforeAndAfterEach with Logging {
  self: Suite with ScalaFutures with AppendedClues =>

  //longer timeout for dropping database or cleaning collections
  private val longPatienceConfig = PatienceConfig(
    timeout  = scaled(Span(6, Seconds)),
    interval = scaled(Span(50, Millis)) //tests aren't run in parallel so why bother with waiting longer
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    clearAllCollectionsButRetainIndices()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    dropMongoDb()
  }

  def dropMongoDb()(implicit ec: ExecutionContext = global): Unit = {
    logger.info("dropping database ...")
    mongo().drop().futureValue(longPatienceConfig, implicitly[Position]) withClue "dropping database failed"
  }

  def clearAllCollectionsButRetainIndices()(implicit ec: ExecutionContext = global): Unit = {
    import ImplicitBSONHandlers._
    logger.info("clearing collections ...")
    val dropF =
      for {
        collNames <- mongo().collectionNames
        collections = collNames.map(name => mongo().collection[JSONCollection](name))
        _ <- Future.sequence(collections.map(_.delete(ordered = true).one(Json.obj(), None)))
      } yield ()

    dropF.futureValue(longPatienceConfig, implicitly[Position]) withClue "dropping collections failed"
  }
}
