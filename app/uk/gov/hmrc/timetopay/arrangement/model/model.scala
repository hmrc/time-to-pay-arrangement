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

package uk.gov.hmrc.timetopay.arrangement.model

import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.util.UUID
import scala.language.implicitConversions

case class PaymentSchedule(
    startDate:            LocalDate,
    endDate:              LocalDate,
    initialPayment:       BigDecimal,
    amountToPay:          BigDecimal,
    instalmentBalance:    BigDecimal,
    totalInterestCharged: BigDecimal,
    totalPayable:         BigDecimal,
    instalments:          List[Instalment])

object PaymentSchedule {
  implicit val format: OFormat[PaymentSchedule] = Json.format
}

case class Instalment(paymentDate: LocalDate, amount: BigDecimal)

object Instalment {
  implicit val format: OFormat[Instalment] = Json.format
}

case class Taxpayer(
    customerName:   String,
    addresses:      List[Address],
    selfAssessment: SelfAssessment)

object Taxpayer {
  implicit val format: OFormat[Taxpayer] = Json.format
}

case class SelfAssessment(
    utr:                      String,
    communicationPreferences: Option[CommunicationPreferences],
    debits:                   List[Debit])

object SelfAssessment {
  implicit val format: OFormat[SelfAssessment] = Json.format
}

case class Address(
    addressLine1: String         = "",
    addressLine2: Option[String] = None,
    addressLine3: Option[String] = None,
    addressLine4: Option[String] = None,
    addressLine5: Option[String] = None,
    postcode:     Option[String] = None)

object Address {
  implicit val format: OFormat[Address] = Json.format
}

case class CommunicationPreferences(
    welshLanguageIndicator: Boolean,
    audioIndicator:         Boolean,
    largePrintIndicator:    Boolean,
    brailleIndicator:       Boolean)

object CommunicationPreferences {
  implicit val format: OFormat[CommunicationPreferences] = Json.format
}

case class Debit(originCode: String, dueDate: LocalDate)

object Debit {
  implicit val format: OFormat[Debit] = Json.format
}

case class DesDebit(debitType: String, dueDate: LocalDate)

object DesDebit {
  implicit val format: OFormat[DesDebit] = Json.format
}

case class TTPArrangement(
    _id:                  String                       = UUID.randomUUID().toString,
    paymentPlanReference: String,
    directDebitReference: String,
    taxpayer:             Taxpayer,
    bankDetails:          BankDetails,
    schedule:             PaymentSchedule,
    desArrangement:       Option[DesSubmissionRequest])
object TTPArrangement {
  implicit val ttpArrangementFormat: OFormat[TTPArrangement] = Json.format[TTPArrangement]
}

case class DesTTPArrangement(
    startDate:               LocalDate,
    endDate:                 LocalDate,
    firstPaymentDate:        LocalDate,
    firstPaymentAmount:      String,
    regularPaymentAmount:    String,
    regularPaymentFrequency: String         = "Monthly",
    reviewDate:              LocalDate,
    initials:                String         = "ZZZ",
    enforcementAction:       String,
    directDebit:             Boolean        = true,
    debitDetails:            List[DesDebit],
    saNote:                  String)

object DesTTPArrangement {
  implicit val format: OFormat[DesTTPArrangement] = Json.format
}

case class LetterAndControl(
    customerName:       String,
    salutation:         String         = "Dear Sir or Madam",
    addressLine1:       String         = "",
    addressLine2:       Option[String] = None,
    addressLine3:       Option[String] = None,
    addressLine4:       Option[String] = None,
    addressLine5:       Option[String] = None,
    postCode:           Option[String] = None,
    totalAll:           String,
    clmIndicateInt:     String         = "Interest is due",
    clmPymtString:      String,
    officeName1:        String         = "",
    officeName2:        String         = "",
    officePostcode:     String         = "",
    officePhone:        String         = "",
    officeFax:          String         = "",
    officeOpeningHours: String         = "9-5",
    template:           String         = "template",
    exceptionType:      Option[String] = None,
    exceptionReason:    Option[String] = None)

object LetterAndControl {
  implicit val format: OFormat[LetterAndControl] = Json.format
}

case class DesSubmissionRequest(ttpArrangement: DesTTPArrangement, letterAndControl: LetterAndControl)

object DesSubmissionRequest {
  implicit val format: OFormat[DesSubmissionRequest] = Json.format
}

object modelFormat {

  implicit val localDateFormat: Format[LocalDate] = new Format[LocalDate] {
    override def reads(json: JsValue): JsResult[LocalDate] =
      json.validate[String].map(LocalDate.parse)

    override def writes(o: LocalDate): JsValue = Json.toJson(o.toString)
  }
  implicit val localDateTimeFormat: Format[LocalDateTime] = new Format[LocalDateTime] {
    override def reads(json: JsValue): JsResult[LocalDateTime] =
      json.validate[String].map(LocalDateTime.parse)

    override def writes(o: LocalDateTime): JsValue = Json.toJson(o.toString)
  }

  implicit val instalmentFormat: OFormat[Instalment] = Json.format[Instalment]
  implicit val scheduleFormat: OFormat[PaymentSchedule] = Json.format[PaymentSchedule]
  implicit val addressFormat: OFormat[Address] = Json.format[Address]
  implicit val desDebitFormat: OFormat[DesDebit] = Json.format[DesDebit]
  implicit val debitFormat: OFormat[Debit] = Json.format[Debit]

  implicit val communicationPreferencesFormat: OFormat[CommunicationPreferences] = Json.format[CommunicationPreferences]
  implicit val selfAssessmentFormat: OFormat[SelfAssessment] = Json.format[SelfAssessment]
  implicit val taxPayerFormat: OFormat[Taxpayer] = Json.format[Taxpayer]
  implicit val desTTArrangementFormat: OFormat[DesTTPArrangement] = Json.format[DesTTPArrangement]
  implicit val letterAndControlFormat: OFormat[LetterAndControl] = Json.format[LetterAndControl]
  implicit val desSubmissionRequestFormat: OFormat[DesSubmissionRequest] = Json.format[DesSubmissionRequest]

}

//case class AnonymousTTPArrangement(
//    _id:                  String,
//    createdOn:            LocalDateTime,
//    paymentPlanReference: String,
//    directDebitReference: String,
//    taxpayer:             AnonymousTaxpayer,
//    bankDetails:          BankDetails,
//    schedule:             PaymentSchedule,
//    desArrangement:       Option[AnonymousDesSubmissionRequest])
//
//object AnonymousTTPArrangement {
//  implicit val localDateTimeFormat: Format[LocalDateTime] = {
//    val localDateTimeReads: Reads[LocalDateTime] =
//      Reads.at[String](__ \ "$date" \ "$numberLong")
//        .map(dateTime => Instant.ofEpochMilli(dateTime.toLong).atZone(ZoneOffset.UTC).toLocalDateTime)
//
//    val localDateTimeWrites: Writes[LocalDateTime] =
//      Writes.at[String](__ \ "$date" \ "$numberLong")
//        .contramap(_.toInstant(ZoneOffset.UTC).toEpochMilli.toString)
//
//    Format(localDateTimeReads, localDateTimeWrites)
//  }
//
//  implicit val format: OFormat[AnonymousTTPArrangement] = Json.format[AnonymousTTPArrangement]
//
//  def apply(ttpArrangement: TTPArrangement): AnonymousTTPArrangement = {
//    AnonymousTTPArrangement(
//      _id                  = UUID.randomUUID().toString,
//      createdOn            = LocalDateTime.now(),
//      paymentPlanReference = ttpArrangement.paymentPlanReference,
//      directDebitReference = ttpArrangement.directDebitReference,
//      taxpayer             = AnonymousTaxpayer(ttpArrangement.taxpayer),
//      bankDetails          = ttpArrangement.bankDetails,
//      schedule             = ttpArrangement.schedule,
//      desArrangement       = ttpArrangement.desArrangement match {
//        case None => None
//        case Some(desSubmissionRequest: DesSubmissionRequest) =>
//          Some(AnonymousDesSubmissionRequest(desSubmissionRequest))
//      }
//    )
//  }
//}
//
//case class AnonymousTaxpayer(
//    selfAssessment: AnonymousSelfAssessment)
//
//object AnonymousTaxpayer {
//  implicit val format: OFormat[AnonymousTaxpayer] = Json.format
//
//  def apply(taxpayer: Taxpayer): AnonymousTaxpayer = {
//    AnonymousTaxpayer(selfAssessment = AnonymousSelfAssessment(taxpayer.selfAssessment))
//  }
//}
//
//case class AnonymousSelfAssessment(
//    utr: String
//)
//
//object AnonymousSelfAssessment {
//  implicit val format: OFormat[AnonymousSelfAssessment] = Json.format
//
//  def apply(selfAssessment: SelfAssessment): AnonymousSelfAssessment = {
//    AnonymousSelfAssessment(utr = selfAssessment.utr)
//  }
//}

//case class AnonymousDesSubmissionRequest(ttpArrangement: DesTTPArrangement)
//
//object AnonymousDesSubmissionRequest {
//  implicit val format: OFormat[AnonymousDesSubmissionRequest] = Json.format
//
//  def apply(desSubmissionRequest: DesSubmissionRequest): AnonymousDesSubmissionRequest = {
//    AnonymousDesSubmissionRequest(ttpArrangement = desSubmissionRequest.ttpArrangement)
//  }
//}

case class TTPArrangementResponse(
    _id:                  String,
    paymentPlanReference: String,
    directDebitReference: String,
    taxpayer:             Taxpayer,
    bankDetails:          BankDetails,
    schedule:             PaymentSchedule,
    desArrangement:       Option[DesSubmissionRequestResponse]
)

object TTPArrangementResponse {
  implicit val writes: Writes[TTPArrangementResponse] = Json.writes[TTPArrangementResponse]

  //  def apply(anonymousTTPArrangement: AnonymousTTPArrangement): TTPArrangementResponse = {
  //    TTPArrangementResponse(
  //      _id                  = anonymousTTPArrangement._id,
  //      createdOn            = anonymousTTPArrangement.createdOn.toString,
  //      paymentPlanReference = anonymousTTPArrangement.paymentPlanReference,
  //      directDebitReference = anonymousTTPArrangement.directDebitReference,
  //      taxpayer             = anonymousTTPArrangement.taxpayer,
  //      bankDetails          = anonymousTTPArrangement.bankDetails,
  //      schedule             = anonymousTTPArrangement.schedule,
  //      desArrangement       = anonymousTTPArrangement.desArrangement match {
  //        case None => None
  //        case Some(anonymousDesSubmissionRequest: AnonymousDesSubmissionRequest) =>
  //          Some(DesSubmissionRequestResponse(ttpArrangement = anonymousDesSubmissionRequest.ttpArrangement))
  //      }
  //    )
  //  }
}

case class DesSubmissionRequestResponse(ttpArrangement: DesTTPArrangement)

object DesSubmissionRequestResponse {
  implicit val writes: Writes[DesSubmissionRequestResponse] = {
    (o: DesSubmissionRequestResponse) =>
      Json.obj(
        "ttpArrangement" -> Json.toJson(o.ttpArrangement),
        "letterAndControl" -> Json.obj()
      )
  }
}
