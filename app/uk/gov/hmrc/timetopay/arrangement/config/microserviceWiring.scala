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

import play.api.Play.configuration
import play.api.mvc.Controller
import play.modules.reactivemongo.ReactiveMongoPlugin
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.microservice.connectors.AuthConnector
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.http.{HttpGet, HttpPost}
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.timetopay.arrangement._
import uk.gov.hmrc.timetopay.arrangement.services.{DesTTPArrangementBuilder, _}

import scala.concurrent.Future

object WSHttp extends WSGet with WSPut with WSPost with WSDelete with WSPatch with AppName {
  override val hooks: Seq[HttpHook] = NoneRequired
}

object MicroserviceAuditConnector extends AuditConnector with RunMode {
  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")
}

object MicroserviceAuthConnector extends AuthConnector with ServicesConfig {
  override val authBaseUrl = baseUrl("auth")
}

object DesArrangementApiService extends DesArrangementService with ServicesConfig {
  override val desArrangementUrl: String = baseUrl("des-arrangement-api")
  override val serviceEnvironment: String = getConfString("des-arrangement-api.environment", "unknown")
  override val authorisationToken: String = getConfString("des-arrangement-api.authorization-token", "not-found")

  override val http: HttpGet with HttpPost = WSHttp
}



object RepositoryConfig  {
  private implicit val connection = {
    import play.api.Play.current
    ReactiveMongoPlugin.mongoConnector.db
  }
  lazy val TTPArrangementRepository = new TTPArrangementRepository

}

trait ServiceRegistry extends ServicesConfig {

  import scala.concurrent.ExecutionContext.Implicits.global
  lazy val arrangementDesApiConnector = DesArrangementApiService

  import play.api.Play.current
  lazy val JurisdictionCheckerService = new JurisdictionChecker(JurisdictionCheckerConfig.create(configuration.getConfig("jurisdictionChecker")
    .getOrElse(throw new RuntimeException("LetterAndControl configuration required"))))

  lazy val letterAndControlService = new LetterAndControlBuilder(LetterAndControlConfig.create(configuration.getConfig("letterAndControl")
    .getOrElse(throw new RuntimeException("LetterAndControl configuration required"))),JurisdictionCheckerService) {}

  lazy val desTTPArrangementService = new DesTTPArrangementBuilder(JurisdictionCheckerService)

  lazy val letterAndControl: (TTPArrangement => LetterAndControl) = arrangement => letterAndControlService.create(arrangement)
  lazy val desArrangement: (TTPArrangement => DesTTPArrangement) = arrangement => desTTPArrangementService.create(arrangement)
  lazy val arrangementSave: (TTPArrangement => Future[Option[TTPArrangement]]) = arrangement => RepositoryConfig.TTPArrangementRepository.save(arrangement)
  lazy val arrangementGet: (String => Future[Option[TTPArrangement]]) = id => RepositoryConfig.TTPArrangementRepository.findById(id)
  lazy val desArrangementApi: ((Taxpayer, DesSubmissionRequest) => Future[Either[SubmissionError, SubmissionSuccess]])
        = (taxpayer, desSubmissionRequest) => arrangementDesApiConnector.submitArrangement(taxpayer, desSubmissionRequest)

  lazy val arrangementService: TTPArrangementService = new TTPArrangementService(
    desArrangementApi, desArrangement, letterAndControl, arrangementSave, arrangementGet
  )
}

trait ControllerRegistry {
  registry: ServiceRegistry =>

  implicit val ec =  scala.concurrent.ExecutionContext.Implicits.global
  private lazy val controllers = Map[Class[_], Controller](
    classOf[TTPArrangementController] -> new TTPArrangementController(arrangementService)
  )

  def getController[A](controllerClass: Class[A]): A = controllers(controllerClass).asInstanceOf[A]
}
