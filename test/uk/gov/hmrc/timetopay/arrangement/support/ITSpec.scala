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

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import com.google.inject.AbstractModule
import org.scalatest.time.{Second, Seconds, Span}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.mvc.Result
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.api.{Application, Configuration}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

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
  lazy val overridingsModule = new AbstractModule {
    override def configure(): Unit = ()
  }
  lazy val servicesConfig = fakeApplication().injector.instanceOf[ServicesConfig]
  lazy val config = fakeApplication().injector.instanceOf[Configuration]
  def baseUrl: String = s"http://localhost:$port"

  implicit def fakeRequest: FakeRequest[_] = FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[_]]

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(
    timeout  = scaled(Span(5, Seconds)),
    interval = scaled(Span(1, Second)))

  implicit val hcWithAuthorization: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))

  def httpClient = fakeApplication().injector.instanceOf[HttpClient]

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

  def createBinId: String = {
    System.currentTimeMillis().toString.takeRight(11)
  }

}
