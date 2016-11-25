package uk.gov.hmrc.timetopay.arrangement

import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.mvc.{Result, Action, RequestHeader}
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.timetopay.arrangement.services.{DesApiException, TTPArrangementService}

import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.Future._
import modelFormat._

class TTPArrangementController(arrangementService: TTPArrangementService)(implicit ec: ExecutionContext) extends BaseController {

  def createdNoLocation = Future.successful[Result](Created)
  def createdWithLocation(id: String)(implicit reqHead: RequestHeader) = Future.successful[Result](Created.withHeaders(LOCATION -> s"$protocol://${reqHead.host}/ttparrangements/$id"))

  def create() = Action.async(parse.json) {
    implicit request =>
      withJsonBody[TTPArrangement] {
        arrangement =>
          arrangementService.submit(arrangement).flatMap {
            x => x.fold(createdNoLocation)(a => createdWithLocation(a.id.get))
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
