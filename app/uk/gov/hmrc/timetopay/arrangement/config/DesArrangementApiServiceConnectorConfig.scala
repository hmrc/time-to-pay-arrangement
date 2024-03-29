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

package uk.gov.hmrc.timetopay.arrangement.config

import javax.inject.Inject
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

final case class DesArrangementApiServiceConnectorConfig(desArrangementUrl: String, serviceEnvironment: String, authorisationToken: String) {

  @Inject()
  def this(sConfig: ServicesConfig, configuration: Configuration) =
    this(
      desArrangementUrl  = sConfig.baseUrl("des-arrangement-api"),
      serviceEnvironment = configuration.get[String]("microservice.services.des-arrangement-api.environment"),
      authorisationToken = configuration.get[String]("microservice.services.des-arrangement-api.authorization-token"))

  val desHeaders: Seq[(String, String)] = Seq(
    "Authorization" -> s"Bearer $authorisationToken",
    "Environment" -> serviceEnvironment
  )
}
