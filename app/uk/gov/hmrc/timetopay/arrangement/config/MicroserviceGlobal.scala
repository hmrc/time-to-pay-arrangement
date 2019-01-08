/*
 * Copyright 2019 HM Revenue & Customs
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
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api.{Application, Configuration}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig
import uk.gov.hmrc.play.auth.microservice.connectors.AuthConnector
import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter
import uk.gov.hmrc.play.config._
import uk.gov.hmrc.play.microservice.bootstrap.DefaultMicroserviceGlobal
import uk.gov.hmrc.play.microservice.filters.{AuditFilter, LoggingFilter, MicroserviceFilterSupport}

object MicroserviceGlobal extends DefaultMicroserviceGlobal
    with MicroserviceFilterSupport with DefaultRunMode
{

  object CConfig extends ControllerConfig {
    lazy val controllerConfigs: Config =
      Configuration.load(play.Environment.simple.underlying).
        underlying.as[Config]("controllers")
  }

  private val mconfig = CConfig.paramsForController _

  override val auditConnector: AuditConnector = MicroserviceAuditConnector

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] =
    app.configuration.getConfig(s"$env.microservice.metrics")

  override val loggingFilter: LoggingFilter with MicroserviceFilterSupport =
    new LoggingFilter with MicroserviceFilterSupport {
      override def controllerNeedsLogging(controllerName: String): Boolean =
        mconfig(controllerName).needsLogging
    }

  override val microserviceAuditFilter: AuditFilter with DefaultAppName with MicroserviceFilterSupport =
    new AuditFilter with DefaultAppName with MicroserviceFilterSupport{
      override val auditConnector: AuditConnector = MicroserviceAuditConnector
      override def controllerNeedsAuditing(controllerName: String): Boolean =
        mconfig(controllerName).needsAuditing
    }

  override val authFilter = Some(
    new AuthorisationFilter with MicroserviceFilterSupport {
      override lazy val authParamsConfig: AuthParamsControllerConfig = new AuthParamsControllerConfig {
        def controllerConfigs: Config = CConfig.controllerConfigs
      }
      override lazy val authConnector: AuthConnector = MicroserviceAuthConnector
      override def controllerNeedsAuth(controllerName: String): Boolean =
        mconfig(controllerName).needsAuth
    })
}
