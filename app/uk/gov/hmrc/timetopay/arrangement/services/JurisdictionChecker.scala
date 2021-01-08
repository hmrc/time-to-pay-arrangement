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

package uk.gov.hmrc.timetopay.arrangement.services

import enumeratum.{Enum, EnumEntry}
import javax.inject.Singleton
import uk.gov.hmrc.timetopay.arrangement.Address
import uk.gov.hmrc.timetopay.arrangement.config.JurisdictionCheckerConfig

sealed abstract class JurisdictionType extends EnumEntry {
}

object JurisdictionTypes extends Enum[JurisdictionType] {

  case object English extends JurisdictionType {
  }

  case object Scottish extends JurisdictionType {
  }

  case object Welsh extends JurisdictionType {
  }

  override def values = findValues

}

@Singleton
class JurisdictionChecker(config: JurisdictionCheckerConfig) {

  val scottishPostCodeRegex = config.scottishPrefix.r
  val welshPostCodeRegex = config.welshPrefix.r

  def addressType(address: Address): Option[JurisdictionType] = {
    address.postcode.filterNot(_.trim.isEmpty).map {
      case scottishPostCodeRegex(_) => JurisdictionTypes.Scottish
      case welshPostCodeRegex(_)    => JurisdictionTypes.Welsh
      case _                        => JurisdictionTypes.English
    }
  }
}
