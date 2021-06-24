/*
 * Copyright 2021 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}

@Singleton
class LetterAndControlAndJurisdictionChecker @Inject() (config: Configuration) {
  val logger: Logger = Logger(getClass)

  def createLetterAndControlConfig: LetterAndControlConfig = {
    LetterAndControlConfig(
      getConfString("letterAndControl.salutation", ""),
      getConfString("letterAndControl.claimIndicateInt", ""),
      getConfString("letterAndControl.template", ""),
      getConfString("letterAndControl.office.officeName1", ""),
      getConfString("letterAndControl.office.officeName2", ""),
      getConfString("letterAndControl.office.officePostCode", ""),
      getConfString("letterAndControl.office.officePhone", ""),
      getConfString("letterAndControl.office.officeFax", ""),
      getConfString("letterAndControl.office.officeOpeningHours", ""))
  }

  def getConfString(confKey: String, defString: => String): String = {
    val possibleValue = config.getOptional[String](s"$confKey")
    possibleValue match {
      case Some(x) => x
      case None    => defString
    }

  }
}
