package uk.gov.hmrc.timetopay.arrangement.config

import play.api.mvc.Controller
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.microservice.connectors.AuthConnector
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.http.{HttpPost, HttpGet}
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.timetopay.arrangement.connectors.ArrangementDesApiConnector
import uk.gov.hmrc.timetopay.arrangement.controllers.TTPArrangementController
import uk.gov.hmrc.timetopay.arrangement.models.{LetterAndControl, DesTTPArrangement, TTPArrangement}
import uk.gov.hmrc.timetopay.arrangement.repositories._
import uk.gov.hmrc.timetopay.arrangement.services.{TTPArrangementService, DesTTPArrangementService, LetterAndControlService}

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

object ArrangementDesApiConnector extends ArrangementDesApiConnector with ServicesConfig {

  override val desArrangementUrl: String =  baseUrl("des-arrangement-api")
  override val serviceEnvironment: String = getConfString("des-arrangement-api.environment", "unknown")
  override val authorisationToken: String =getConfString("des-arrangement-api.authorization-token", "not-found")

  override val http: HttpGet with HttpPost = WSHttp
}

object LetterAndControlService extends LetterAndControlService {}

object DesTTPArrangementService extends DesTTPArrangementService {}


trait ServiceRegistry extends ServicesConfig {

  import uk.gov.hmrc.timetopay.arrangement.repositories._

  import scala.concurrent.ExecutionContext.Implicits.global
  lazy val arrangementDesApiConnector = ArrangementDesApiConnector
  lazy val letterAndControlService = LetterAndControlService
  lazy val desTTPArrangementService = DesTTPArrangementService

  lazy val letterAndControl:(TTPArrangement => Future[LetterAndControl]) = arrangement => letterAndControlService.create(arrangement)
  lazy val desArrangement:(TTPArrangement => Future[DesTTPArrangement]) = arrangement => desTTPArrangementService.create(arrangement)
  lazy val arrangementSave: (TTPArrangement => Future[Option[TTPArrangement]]) = arrangement => TTPArrangementRepository.save(arrangement)
  lazy val arrangementGet: (String => Future[Option[TTPArrangement]]) = id => TTPArrangementRepository.findById(id)

  lazy val arrangementService: TTPArrangementService = new TTPArrangementService(
    arrangementDesApiConnector, desArrangement, letterAndControl, arrangementSave, arrangementGet
  )
}

trait ControllerRegistry {
  registry: ServiceRegistry =>

  private lazy val controllers = Map[Class[_], Controller](
    classOf[TTPArrangementController] -> new TTPArrangementController(arrangementService)
  )

  def getController[A](controllerClass: Class[A]) : A = controllers(controllerClass).asInstanceOf[A]
}