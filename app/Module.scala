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

import org.apache.pekko.actor.{ActorSystem, Scheduler}

import java.time.{Clock, ZoneOffset}
import com.google.inject.{AbstractModule, Provides, Singleton}
import uk.gov.hmrc.timetopay.arrangement.services.{PollerService, TTPArrangementJobExtender}
import uk.gov.hmrc.timetopay.arrangement.services.PollerService.OnCompleteAction

class Module extends AbstractModule {

  @Provides
  @Singleton
  def clock(): Clock = Clock.systemDefaultZone.withZone(ZoneOffset.UTC)

  @Provides
  @Singleton
  def scheduler(actorSystem: ActorSystem): Scheduler = actorSystem.scheduler

  @Provides
  @Singleton
  def pollerServiceOnCompleteAction: OnCompleteAction = OnCompleteAction(() => ())

  override def configure(): Unit = {
    bind(classOf[PollerService]).asEagerSingleton()
    bind(classOf[TTPArrangementJobExtender]).asEagerSingleton()
  }
}
