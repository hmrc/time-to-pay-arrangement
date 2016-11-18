package uk.gov.hmrc.timetopay.arrangement

import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, RequestHeader}
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.timetopay.arrangement.services.{DesApiException, TTPArrangementService}

import scala.concurrent.ExecutionContext
import scala.concurrent.Future._
import modelFormat._

class TTPArrangementController(arrangementService: TTPArrangementService)(implicit ec: ExecutionContext) extends BaseController {

  def create() = Action.async(parse.json) {
    implicit request =>
      withJsonBody[TTPArrangement] {
        arrangement =>
          arrangementService.submit(arrangement).map {
            _.map(a => Created.withHeaders(LOCATION -> s"$protocol://${request.host}/ttparrangements/${a.id.get}")).getOrElse(Created)
          }.recover {
            case desApiException: DesApiException =>
              val desFailureMessage: String = s"Submission to DES failed, status code [${desApiException.code}] and response [${desApiException.message}]"
              Logger.error(desFailureMessage)
              InternalServerError(s"$desFailureMessage")
            case failure: Throwable =>
              Logger.error(s"Failed to submit arrangement $failure")
              InternalServerError(failure.getMessage)
          }
      }
  }

  def arrangement(id: String) = Action.async {
    implicit request =>
      Logger.debug(s"Requested arrangement $id")
      arrangementService.byId(id).flatMap {
        _.fold(successful(NotFound(s"arrangement with $id does not exist")))(r => successful(Ok(toJson(r))))
      }
  }

  def protocol(implicit reqHead: RequestHeader) = if (reqHead.secure) "https" else "http"

}
