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

package uk.gov.hmrc.timetopay.arrangement.config

import javax.inject.Inject

import play.api.Configuration


case class JurisdictionCheckerConfig @Inject()(scottishPrefix:String , welshPrefix:String)

object JurisdictionCheckerConfig {

  def create(configuration: Configuration) = {

    def getConfig(key: String) = configuration.getString(key).getOrElse("")

    JurisdictionCheckerConfig(getConfig("scottish.postcode.prefix"),
      getConfig("welsh.postcode.prefix")
    )

  }
}
