package uk.gov.hmrc.timetopay.arrangement.controllers


import play.api.mvc.Action
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.timetopay.arrangement.models.TTPArrangement
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._
import uk.gov.hmrc.timetopay.arrangement.services.TTPArrangementService


import scala.concurrent.ExecutionContext.Implicits.global

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
    }
  }

}
