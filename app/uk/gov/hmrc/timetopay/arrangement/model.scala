/*
 * Copyright 2019 HM Revenue & Customs
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

import java.time.{LocalDateTime, LocalDate}

import play.api.libs.json.{Json, JsResult, JsValue, OFormat, Format}


case class Schedule(startDate: LocalDate,
                    endDate: LocalDate,
                    initialPayment: BigDecimal,
                    amountToPay: BigDecimal,
                    instalmentBalance: BigDecimal,
                    totalInterestCharged: BigDecimal,
                    totalPayable: BigDecimal,
                    instalments: List[Instalment])

case class Instalment (paymentDate: LocalDate, amount: BigDecimal)


case class Taxpayer(customerName: String,
                    addresses: List[Address],
                    selfAssessment: SelfAssessment)

case class SelfAssessment(utr: String,
                          communicationPreferences: Option[CommunicationPreferences],
                          debits: List[Debit])

case class Address(addressLine1: String = "",
                   addressLine2: Option[String] = None,
                   addressLine3: Option[String] = None,
                   addressLine4: Option[String] = None,
                   addressLine5: Option[String] = None,
                   postcode: String = "")

case class CommunicationPreferences(welshLanguageIndicator: Boolean,
                                    audioIndicator: Boolean,
                                    largePrintIndicator: Boolean,
                                    brailleIndicator: Boolean)

case class Debit(originCode: String, dueDate: LocalDate)

case class DesDebit(debitType: String, dueDate: LocalDate)

case class TTPArrangement(id: Option[String],
                          createdOn: Option[LocalDateTime],
                          paymentPlanReference: String,
                          directDebitReference: String,
                          taxpayer: Taxpayer,
                          schedule: Schedule,
                          desArrangement : Option[DesSubmissionRequest])

case class DesTTPArrangement(startDate: LocalDate,
                             endDate: LocalDate,
                             firstPaymentDate: LocalDate,
                             firstPaymentAmount: String,
                             regularPaymentAmount: String,
                             regularPaymentFrequency: String = "Monthly",
                             reviewDate: LocalDate,
                             initials: String = "ZZZ",
                             enforcementAction: String,
                             directDebit: Boolean = true,
                             debitDetails: List[DesDebit],
                             saNote: String)

case class LetterAndControl(customerName: String,
                            salutation: String = "Dear Sir or Madam",
                            addressLine1: String = "",
                            addressLine2: Option[String] = None,
                            addressLine3: Option[String] = None,
                            addressLine4: Option[String] = None,
                            addressLine5: Option[String] = None,
                            postCode: String = "",
                            totalAll: String,
                            clmIndicateInt: String = "Interest is due",
                            clmPymtString: String,
                            officeName1: String = "",
                            officeName2: String = "",
                            officePostcode: String = "",
                            officePhone: String = "",
                            officeFax: String = "",
                            officeOpeningHours: String = "9-5",
                            template: String = "template",
                            exceptionType: Option[String] = None,
                            exceptionReason: Option[String] = None
                           )

case class DesSubmissionRequest(ttpArrangement: DesTTPArrangement, letterAndControl: LetterAndControl)

object modelFormat {

  implicit val localDateFormat = new Format[LocalDate] {
    override def reads(json: JsValue): JsResult[LocalDate] =
      json.validate[String].map(LocalDate.parse)

    override def writes(o: LocalDate): JsValue = Json.toJson(o.toString)
  }
  implicit val localDateTimeFormat = new Format[LocalDateTime] {
    override def reads(json: JsValue): JsResult[LocalDateTime] =
      json.validate[String].map(LocalDateTime.parse)

    override def writes(o: LocalDateTime): JsValue = Json.toJson(o.toString)
  }

  //AJER check that we do want to switch to OFORMAT
  implicit val instalmentFormat = Json.format[Instalment]
  implicit val scheduleFormat = Json.format[Schedule]
  implicit val addressFormat = Json.format[Address]
  implicit val desDebitFormat = Json.format[DesDebit]
  implicit val debitFormat = Json.format[Debit]

  implicit val communicationPreferencesFormat = Json.format[CommunicationPreferences]
  implicit val selfAssessmentFormat = Json.format[SelfAssessment]
  implicit val taxPayerFormat = Json.format[Taxpayer]
  implicit val desTTArrangementFormat = Json.format[DesTTPArrangement]
  implicit val letterAndControlFormat = Json.format[LetterAndControl]
  implicit val desSubmissionRequestFormat = Json.format[DesSubmissionRequest]
  implicit val ttpArrangementFormat = Json.format[TTPArrangement]
  //implicit val ttpArrangementOFormat: OFormat[TTPArrangement] = Json.format[TTPArrangement]
}
