/*
 * Copyright 2017 HM Revenue & Customs
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
import javax.inject.{Inject, Provider}

import com.google.inject.{AbstractModule, Singleton}
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api.{Application, Configuration, Play}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.DB
import uk.gov.hmrc.play.audit.filters.AuditFilter
import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig
import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter
import uk.gov.hmrc.play.config._
import uk.gov.hmrc.play.filters.MicroserviceFilterSupport
import uk.gov.hmrc.play.http.logging.filters.LoggingFilter
import uk.gov.hmrc.play.microservice.bootstrap.DefaultMicroserviceGlobal

@Singleton
class ControllerConfiguration @Inject()(
  configuration:Configuration
) extends ControllerConfig {
  lazy val controllerConfigs =
    configuration.underlying.as[Config]("controllers")
}

class GuiceModule extends AbstractModule with ServicesConfig {
  override def configure: Unit = {
    bind(classOf[DB]).toProvider(classOf[MongoDbProvider])
    ()
  }
}

class MongoDbProvider @Inject() (
  reactiveMongoComponent: ReactiveMongoComponent
) extends Provider[DB] {
  def get = reactiveMongoComponent.mongoConnector.db()
}

object MicroserviceGlobal extends DefaultMicroserviceGlobal
    with RunMode with MicroserviceFilterSupport
{

  lazy val controllerConfiguration = new ControllerConfiguration(
    Configuration.load(play.Environment.simple().underlying()))

  private val mconfig = controllerConfiguration.paramsForController _

  override val auditConnector = MicroserviceAuditConnector

  override def microserviceMetricsConfig(implicit app: Application) =
    app.configuration.getConfig(s"$env.microservice.metrics")

  override val loggingFilter =
    new LoggingFilter with MicroserviceFilterSupport {
      override def controllerNeedsLogging(controllerName: String) =
        mconfig(controllerName).needsLogging
    }

  override val microserviceAuditFilter =
    new AuditFilter with AppName with MicroserviceFilterSupport{
      override val auditConnector = MicroserviceAuditConnector
      override def controllerNeedsAuditing(controllerName: String) =
        mconfig(controllerName).needsAuditing
    }

  override val authFilter = Some(
    new AuthorisationFilter with MicroserviceFilterSupport {
      override lazy val authParamsConfig = new AuthParamsControllerConfig {
        def controllerConfigs = controllerConfiguration.controllerConfigs
      }
      override lazy val authConnector = MicroserviceAuthConnector
      override def controllerNeedsAuth(controllerName: String): Boolean =
        mconfig(controllerName).needsAuth
    })
}
