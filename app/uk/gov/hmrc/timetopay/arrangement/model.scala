/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDate

import play.api.libs.json.{Json, OFormat}
import timetopayarrangement.SaUtr

case class Schedule(
                     startDate:            LocalDate,
                     endDate:              LocalDate,
                     initialPayment:       BigDecimal,
                     amountToPay:          BigDecimal,
                     instalmentBalance:    BigDecimal,
                     totalInterestCharged: BigDecimal,
                     totalPayable:         BigDecimal,
                     instalments:          List[Instalment]
                   )

  case class Instalment(paymentDate: LocalDate, amount: BigDecimal)

case class TaxpayerDetails(
    customerName:   String,
    addresses:      Seq[Address],
    selfAssessment: SelfAssessment
)

case class SelfAssessment(
    utr:                      SaUtr,
    communicationPreferences: Option[CommunicationPreferences],
    debits:                   List[Debit]
)

case class Address(
    addressLine1: String         = "",
    addressLine2: Option[String] = None,
    addressLine3: Option[String] = None,
    addressLine4: Option[String] = None,
    addressLine5: Option[String] = None,
    postcode:     String         = ""
)

case class CommunicationPreferences(
    welshLanguageIndicator: Boolean,
    audioIndicator:         Boolean,
    largePrintIndicator:    Boolean,
    brailleIndicator:       Boolean
)

case class Debit(originCode: String, dueDate: LocalDate)





