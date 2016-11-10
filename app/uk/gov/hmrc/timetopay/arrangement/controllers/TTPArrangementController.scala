package uk.gov.hmrc.timetopay.arrangement.controllers


import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Action}
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.timetopay.arrangement.models.TTPArrangement
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._
import uk.gov.hmrc.timetopay.arrangement.services.TTPArrangementService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

class TTPArrangementController(arrangementService: TTPArrangementService) extends BaseController {

  def create() = Action.async(parse.json) {
    implicit request =>
      withJsonBody[TTPArrangement] {
        arrangement =>
          arrangementService.submit(arrangement).map {
            _.map(a => Created.withHeaders(LOCATION -> s"$protocol://${request.host}/ttparrangements/${a.id.get}")).getOrElse(Created)
          }.recover {
            case failure =>
              Logger.error("An exception occurred ", failure)
              InternalServerError(s"A server error occurred: $failure")
          }
      }
  }

  def arrangement(id: String) = Action.async {
    implicit request =>
      Logger.debug(s"Requested arrangement $id")
      arrangementService.byId(id)
        .flatMap(_.map(arrangement => successful(Ok(Json.toJson(arrangement))))
          .getOrElse(successful(NotFound)))
  }

  def protocol(implicit reqHead: RequestHeader) = if (reqHead.secure) "https" else "http"

}
