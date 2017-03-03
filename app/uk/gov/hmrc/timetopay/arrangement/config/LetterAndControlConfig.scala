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

import com.google.inject.Inject
import play.api.Configuration

case class LetterAndControlConfig @Inject()(salutation: String,
                                   claimIndicateInt: String,
                                   template: String,
                                   officeName1: String,
                                   officeName2: String,
                                   officePostCode: String,
                                   officePhone: String,
                                   officeFax: String,
                                   officeOpeningHours: String ){
}
object LetterAndControlConfig {

  def create(configuration: Configuration) = {

    def getConfig(key: String) = configuration.getString(key)
      .getOrElse(throw new IllegalArgumentException(s"Missing $key"))

    LetterAndControlConfig(getConfig("salutation"),
      getConfig("claimIndicateInt") ,
      getConfig("template"),
      getConfig("office.officeName1"),
      getConfig("office.officeName2"),
      getConfig("office.officePostCode"),
      getConfig("office.officePhone"),
      getConfig("office.officeFax"),
      getConfig("office.officeOpeningHours")
    )

  }
}
