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


object TestGetTTPArrangementController extends TestGetTTPArrangementController {
  override val arrangementService = TTPArrangementService
}

trait TestGetTTPArrangementController extends BaseController {

  val arrangementService: TTPArrangementService

  def arrangement(id: String) = Action.async {
    implicit request =>
      Logger.debug(s"Requested arrangement $id")
      val future: Future[Option[TTPArrangement]] = arrangementService.byId(id)
      future.flatMap(x => x.map(arrangement => successful(Ok(Json.toJson(arrangement)))).getOrElse(successful(NotFound)))
  }
}
