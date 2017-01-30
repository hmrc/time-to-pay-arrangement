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

package uk.gov.hmrc.timetopay.arrangement.services

import play.api.Play.application
import uk.gov.hmrc.timetopay.arrangement.Address


object JurisdictionType extends Enumeration  {
  type JurisdictionType = Value
  val English, Scottish, Welsh = Value
}

object JurisdictionChecker {
  import uk.gov.hmrc.timetopay.arrangement.services.JurisdictionType._
  import play.api.Play.current

  val scottishPostCodeRegex = application.configuration.getString("scottish.postcode.prefix")
    .getOrElse(throw new RuntimeException("Scottish postcode prefix needed")).r
  val welshPostCodeRegex = application.configuration.getString("welsh.postcode.prefix")
    .getOrElse(throw new RuntimeException("Welsh postcode prefix needed")).r

  def addressType(address: Address): JurisdictionType = {
     address.postcode match {
       case scottishPostCodeRegex(_) => Scottish
       case welshPostCodeRegex(_) => Welsh
       case _ =>  English
     }
  }
}
