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
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoDbConnection
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHooks
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.microservice.connectors.AuthConnector
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.play.microservice.config.LoadAuditingConfig
import uk.gov.hmrc.timetopay.arrangement._
import uk.gov.hmrc.timetopay.arrangement.services._

import scala.concurrent.ExecutionContextExecutor

trait Hooks extends HttpHooks with HttpAuditing{
  override val hooks: Seq[AuditingHook.type] = Seq(AuditingHook)
  override lazy val auditConnector: MicroserviceAuditConnector.type = MicroserviceAuditConnector
}

trait WSHttp extends WSGet with HttpGet with WSPost with HttpPost with WSDelete with HttpDelete  with WSPatch with HttpPatch with Hooks with DefaultAppName {
  override lazy val configuration: Option[Config] = None
}

object WSHttp extends WSHttp

object MicroserviceAuditConnector extends AuditConnector with DefaultRunMode {
  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")
}

object MicroserviceAuthConnector extends AuthConnector with ServicesConfig with WSHttp with DefaultRunMode {
  override val authBaseUrl: String = baseUrl("auth")
}

class DesArrangementApiService() extends DesArrangementService with ServicesConfig with DefaultRunMode {
  override val desArrangementUrl: String = baseUrl("des-arrangement-api")
  override val serviceEnvironment: String = getConfString("des-arrangement-api.environment", "unknown")
  override val authorisationToken: String = getConfString("des-arrangement-api.authorization-token", "not-found")

  override val http: HttpGet with HttpPost = WSHttp
}

class LetterAndControlAndJurisdictionChecker extends ServicesConfig with DefaultRunMode {
  override def getConfString(confKey: String, defString: => String): String = {
    runModeConfiguration.getString(s"$confKey").
      getOrElse(runModeConfiguration.getString(s"$confKey").
        getOrElse(runModeConfiguration.getString(s"$confKey").
          getOrElse(defString)))
  }
  def createLetterAndControlConfig:LetterAndControlConfig = {
    LetterAndControlConfig(
      getConfString("letterAndControl.salutation", ""),
      getConfString("letterAndControl.claimIndicateInt", ""),
      getConfString("letterAndControl.template", ""),
      getConfString("letterAndControl.office.officeName1", ""),
      getConfString("letterAndControl.office.officeName2", ""),
      getConfString("letterAndControl.office.officePostCode", ""),
      getConfString("letterAndControl.office.officePhone", ""),
      getConfString("letterAndControl.office.officeFax", ""),
      getConfString("letterAndControl.office.officeOpeningHours", "")
    )
  }
  def createJurisdictionCheckerConfig:JurisdictionChecker = {
    new JurisdictionChecker(JurisdictionCheckerConfig(
      getConfString("jurisdictionChecker.scottish.postcode.prefix ", ""),
      getConfString("jurisdictionChecker.welsh.postcode.prefix", "")
    ))
  }
}

trait ServiceRegistry extends ServicesConfig with MongoDbConnection with DefaultRunMode {
  val letterAndJurisction = new LetterAndControlAndJurisdictionChecker()
   val TTPArrangementRepository: TTPArrangementRepository =new TTPArrangementRepository(db.apply())
   val arrangementDesApiConnector = new DesArrangementApiService()
   val letterAndControlService = new LetterAndControlBuilder(letterAndJurisction)
   val desTTPArrangementService = new DesTTPArrangementBuilder(letterAndJurisction)
}

trait ControllerRegistry extends ServiceRegistry {

  implicit val ec: ExecutionContextExecutor =  scala.concurrent.ExecutionContext.Implicits.global
  private lazy val controllers = Map[Class[_], Controller](
    classOf[TTPArrangementController] -> new TTPArrangementController(new TTPArrangementService(desTTPArrangementService,
      arrangementDesApiConnector,TTPArrangementRepository,letterAndControlService))
  )

  def getController[A](controllerClass: Class[A]): A = controllers(controllerClass).asInstanceOf[A]
}
