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
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoDbConnection
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.microservice.connectors.AuthConnector
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.play.http.{HttpGet, HttpPost}
import uk.gov.hmrc.timetopay.arrangement.services._
import uk.gov.hmrc.timetopay.arrangement.{TTPArrangementRepository, _}
object WSHttp extends WSGet with WSPut with WSPost with WSDelete with WSPatch with AppName {
  override val hooks: Seq[HttpHook] = NoneRequired
}

object MicroserviceAuditConnector extends AuditConnector with RunMode {
  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")
}

object MicroserviceAuthConnector extends AuthConnector with ServicesConfig {
  override val authBaseUrl = baseUrl("auth")
}

class DesArrangementApiService() extends DesArrangementService with ServicesConfig {
  override val desArrangementUrl: String = baseUrl("des-arrangement-api")
  override val serviceEnvironment: String = getConfString("des-arrangement-api.environment", "unknown")
  override val authorisationToken: String = getConfString("des-arrangement-api.authorization-token", "not-found")

  override val http: HttpGet with HttpPost = WSHttp
}

class LetterAndControlAndJurisdictionChecker extends ServicesConfig {
  override def getConfString(confKey: String, defString: => String) = {
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

trait ServiceRegistry extends ServicesConfig with MongoDbConnection{
  val letterAndJurisction = new LetterAndControlAndJurisdictionChecker()
   val TTPArrangementRepository: TTPArrangementRepository =new TTPArrangementRepository(db.apply())
   val arrangementDesApiConnector = new DesArrangementApiService()
   val letterAndControlService = new LetterAndControlBuilder(letterAndJurisction)
   val desTTPArrangementService = new DesTTPArrangementBuilder(letterAndJurisction)

}

trait ControllerRegistry extends ServiceRegistry {

  implicit val ec =  scala.concurrent.ExecutionContext.Implicits.global
  private lazy val controllers = Map[Class[_], Controller](
    classOf[TTPArrangementController] -> new TTPArrangementController(new TTPArrangementService(desTTPArrangementService,
      arrangementDesApiConnector,TTPArrangementRepository,letterAndControlService))
  )

  def getController[A](controllerClass: Class[A]): A = controllers(controllerClass).asInstanceOf[A]
}
