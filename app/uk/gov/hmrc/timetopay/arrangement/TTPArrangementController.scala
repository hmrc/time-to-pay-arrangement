/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.timetopay.arrangement

import javax.inject.Inject

import play.api.Logger
import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, RequestHeader, Result}
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.timetopay.arrangement.services.{DesApiException, TTPArrangementService}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future._
import modelFormat._

class TTPArrangementController @Inject()(val arrangementService: TTPArrangementService)(implicit ec: ExecutionContext) extends BaseController {

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
