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

package tasks

import play.api.Logging
import play.api.inject._
import uk.gov.hmrc.mongo.MongoComponent

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global

class TasksModule extends SimpleModule(bind[DropCollectionsTask].toSelf.eagerly())

@Singleton
class DropCollectionsTask @Inject() (mongoComponent: MongoComponent) extends Logging {
  logger.info("**************** Start cleanup task: drop alerts_received mongodb collections...")

  mongoComponent.client
    .getDatabase("time-to-pay-arrangement")
    .getCollection("ttparrangements")
    .drop()
    .toFuture()
    .map { _ => logger.info("**************** cleanup done.")
    }
}

