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

package uk.gov.hmrc.timetopay.arrangement.support

import com.codahale.metrics.SharedMetricRegistries
import com.github.pjfanning.pekko.scheduler.mock.VirtualTime
import com.google.inject.{AbstractModule, Provides, Singleton}
import org.apache.pekko.actor.Scheduler
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Second, Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.api.{Application, Configuration}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.timetopay.arrangement.services.PollerService.OnCompleteAction

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import scala.concurrent.ExecutionContext

/**
 * This is common spec for every test case which brings all of useful routines we want to use in our scenarios.
 */

trait ITSpec
  extends AnyFreeSpecLike
  with RichMatchers
  with BeforeAndAfterEach
  with GuiceOneServerPerSuite
  with WireMockSupport
  with Matchers
  with TestMongoSupport {

  lazy val frozenZonedDateTime: ZonedDateTime = {
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    LocalDateTime.parse("2018-11-02T16:28:55.185", formatter).atZone(ZoneId.of("Europe/London"))
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    SharedMetricRegistries.clear()
  }

  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val virtualTime = new VirtualTime()

  class PollerServiceOnCompleteListener {
    private var _hasCompleted = false

    val onCompleteAction = OnCompleteAction(() => _hasCompleted = true)

    def hasCompleted(): Boolean = _hasCompleted

    def reset(): Unit = _hasCompleted = false

  }

  val pollerServiceOnCompleteListener = new PollerServiceOnCompleteListener

  lazy val overridingsModule = new AbstractModule {
    @Provides
    @Singleton
    def scheduler(): Scheduler = virtualTime.scheduler

    @Provides
    @Singleton
    def pollerServiceOnCompleteAction: OnCompleteAction = pollerServiceOnCompleteListener.onCompleteAction

    override def configure(): Unit = ()
  }

  lazy val servicesConfig = app.injector.instanceOf[ServicesConfig]

  lazy val config = app.injector.instanceOf[Configuration]

  def baseUrl: String = s"http://localhost:$port"

  implicit def fakeRequest: FakeRequest[_] = FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[_]]

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout  = scaled(Span(5, Seconds)),
    interval = scaled(Span(1, Second)))

  implicit val hcWithAuthorization: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))

  def httpClient = app.injector.instanceOf[HttpClientV2]

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(GuiceableModule.fromGuiceModules(Seq(overridingsModule)))
    .configure(Map[String, Any](
      "mongodb.uri" -> mongoUri,
      "metrics.enabled" -> false,
      "metrics.jvm" -> false,
      "microservice.services.des-arrangement-api.host" -> "localhost",
      "microservice.services.des-arrangement-api.environment" -> "localhost",
      "microservice.services.des-arrangement-api.port" -> WireMockSupport.port,
      "microservice.services.auth.port" -> WireMockSupport.port
    )).build()

  def createBinId: String = System.currentTimeMillis().toString.takeRight(11)

}
