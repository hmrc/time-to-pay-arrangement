package uk.gov.hmrc.timetopay.arrangement.controllers


import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.timetopay.arrangement.models.TTPArrangement
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._
import uk.gov.hmrc.timetopay.arrangement.services.TTPArrangementService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

object TTPArrangementController extends TTPArrangementController {

  override val arrangementService = TTPArrangementService

}

trait TTPArrangementController extends BaseController {

  val arrangementService: TTPArrangementService

  def create() = Action.async(parse.json) {
    implicit request =>
      withJsonBody[TTPArrangement] {
        val scheme = if (request.secure) "https://" else "http://"
        arrangement =>
          arrangementService.submit(arrangement).map(response =>
            Created.withHeaders(LOCATION -> s"$scheme${request.host}/ttparrangements/${response.identifier.get}"))
            .recover {
              case failure => InternalServerError(s"A server error occurred: $failure")
            }
      }
  }

  def arrangement(id: String) = Action.async {
    implicit request =>
      Logger.debug(s"Requested arrangement $id")
      val future: Future[Option[TTPArrangement]] = arrangementService.byId(id)
      future.flatMap(x => x.map(arrangement => successful(Ok(Json.toJson(arrangement)))).getOrElse(successful(NotFound)))
  }

}
