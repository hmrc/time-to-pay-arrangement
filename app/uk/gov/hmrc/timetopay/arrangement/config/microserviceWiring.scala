package uk.gov.hmrc.timetopay.arrangement.config

import play.api.Play.configuration
import play.api.mvc.Controller
import play.modules.reactivemongo.ReactiveMongoPlugin
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.microservice.connectors.AuthConnector
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.http.{HttpPost, HttpGet}
import uk.gov.hmrc.play.http.hooks.HttpHook
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.timetopay.arrangement._
import uk.gov.hmrc.timetopay.arrangement.services._

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

object DesArrangementApiService$ extends DesArrangementService with ServicesConfig {
  override val desArrangementUrl: String = baseUrl("des-arrangement-api")
  override val serviceEnvironment: String = getConfString("des-arrangement-api.environment", "unknown")
  override val authorisationToken: String = getConfString("des-arrangement-api.authorization-token", "not-found")

  override val http: HttpGet with HttpPost = WSHttp
}

object DesTTPArrangementBuilder extends DesTTPArrangementBuilder {}

object RepositoryConfig  {
  private implicit val connection = {
    import play.api.Play.current
    ReactiveMongoPlugin.mongoConnector.db
  }
  lazy val TTPArrangementRepository = new TTPArrangementRepository

}

trait ServiceRegistry extends ServicesConfig {

  import scala.concurrent.ExecutionContext.Implicits.global
  lazy val arrangementDesApiConnector = DesArrangementApiService$

  import play.api.Play.current
  lazy val letterAndControlService = new LetterAndControlBuilder(LetterAndControlConfig.create(configuration.getConfig("letterAndControl")
    .getOrElse(throw new RuntimeException("LetterAndControl configuration required")))) {}

  lazy val desTTPArrangementService = DesTTPArrangementBuilder

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