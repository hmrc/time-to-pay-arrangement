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

package uk.gov.hmrc.timetopay.arrangement.config

import java.util.concurrent.TimeUnit

import javax.inject.Inject
import play.api.Configuration

import scala.concurrent.duration.{Duration, FiniteDuration}

final case class QueueConfig(retryAfter:   String,
                             availableFor: Duration,
                             ttl:          Duration,
                             initialDelay: FiniteDuration,
                             interval:     FiniteDuration) {

  @Inject()
  def this(configuration: Configuration) {

    this(
      retryAfter   = "queue.retryAfter",
      availableFor = configuration.get[Duration]("queue.available.for"),
      ttl          = configuration.get[Duration]("queue.ttl"),
      initialDelay = FiniteDuration(configuration.get[Duration]("poller.initialDelay").toNanos, TimeUnit.NANOSECONDS),
      interval     = FiniteDuration(configuration.get[Duration]("poller.interval").toNanos, TimeUnit.NANOSECONDS)
    )
  }
}
