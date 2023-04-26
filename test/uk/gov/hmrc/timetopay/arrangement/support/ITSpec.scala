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
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers

import org.scalatestplus.play.guice.GuiceOneServerPerTest
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.mvc.Result
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.api.{Application, Configuration}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
 * This is common spec for every test case which brings all of useful routines we want to use in our scenarios.
 */

trait ITSpec
  extends AnyFreeSpecLike
  with RichMatchers
  with BeforeAndAfterEach
  with GuiceOneServerPerTest
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
  lazy val servicesConfig = fakeApplication.injector.instanceOf[ServicesConfig]
  lazy val config = fakeApplication.injector.instanceOf[Configuration]
  val baseUrl: String = s"http://localhost:$port"

  implicit def fakeRequest: FakeRequest[_] = FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[_]]

  override implicit val patienceConfig = PatienceConfig(
    timeout  = scaled(Span(3, Seconds)),
    interval = scaled(Span(300, Millis)))

  implicit def emptyHC = HeaderCarrier()

  def httpClient = fakeApplication().injector.instanceOf[HttpClient]

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(GuiceableModule.fromGuiceModules(Seq(overridingsModule)))
    .configure(Map[String, Any](
      "mongodb.uri" -> mongoUri,
      "metrics.enabled" -> false,
      "metrics.jvm" -> false,
      //      "microservice.metrics.graphite.enabled" -> false,
      "microservice.services.des-arrangement-api.host" -> "localhost",
      "microservice.services.des-arrangement-api.environment" -> "localhost",
      "microservice.services.des-arrangement-api.port" -> WireMockSupport.port)).build()

  def createBinId = {
    System.currentTimeMillis().toString.takeRight(11)
  }

  def await[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)

  def status(of: Future[Result])(implicit timeout: Duration): Int = status(Await.result(of, timeout))

  def status(of: Result) = of.header.status

}
