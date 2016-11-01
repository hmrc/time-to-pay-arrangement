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
      arrangement =>
        arrangementService.submit(arrangement).map(response => Created)
    }
  }

}
