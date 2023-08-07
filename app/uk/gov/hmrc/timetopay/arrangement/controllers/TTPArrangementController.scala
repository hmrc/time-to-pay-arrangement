/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.timetopay.arrangement.controllers

import javax.inject.Inject
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.timetopay.arrangement.model.{TTPArrangement, TTPArrangementResponse}
import uk.gov.hmrc.timetopay.arrangement.services.{DesApiException, TTPArrangementService}

import scala.concurrent.Future._
import scala.concurrent.{ExecutionContext, Future}

class TTPArrangementController @Inject() (arrangementService: TTPArrangementService, cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends BackendController(cc) with Logging {

  /**
   * Turns the json into our representation of a TTPArrangement
   * It calls the submit method and applys a location to the returning result.
   */
  val create: Action[JsValue] = Action.async(parse.json) {
    implicit request =>

      withJsonBody[TTPArrangement] {
        arrangement =>
          arrangementService.submit(arrangement).flatMap {
            x => x.fold(createdNoLocation)(a => createdWithLocation(a._id))
          }.recover {
            case desApiException: DesApiException =>
              val desFailureMessage: String = s"Submission to DES failed, status code [${desApiException.code.toString}] and response [${desApiException.message}]" +
                s". Queued for retry: ${desApiException.queuedForRetry.toString}"
              logger.error(desFailureMessage)
              InternalServerError(s"$desFailureMessage")
            case failure =>
              logger.error("Failed to submit arrangement", failure)
              InternalServerError(failure.getMessage)
          }
      }
  }

  private def createdNoLocation = Future.successful[Result](Created)

  private def createdWithLocation(id: String)(implicit reqHead: RequestHeader) = {
    Future.successful[Result](Created.withHeaders(LOCATION -> s"$protocol://${reqHead.host}/ttparrangements/$id"))
  }

  def protocol(implicit reqHead: RequestHeader): String = if (reqHead.secure) "https" else "http"

  def arrangement(id: String): Action[AnyContent] = Action.async {
    _ =>
      // implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

      logger.debug(s"Requested arrangement $id")
      arrangementService.byId(id).flatMap {
        _.fold(
          successful(NotFound(s"arrangement with $id does not exist"))
        )(r => successful(Ok(toJson(TTPArrangementResponse(r)))))
      }
  }
}
