package uk.gov.hmrc.timetopay.arrangement

import java.time.LocalDate

import play.api.libs.json.{Format, JsResult, JsValue, Json}
import uk.gov.hmrc.timetopay.arrangement.models._

package object modelsFormat {

  implicit val localDateFormat = new Format[LocalDate] {
    override def reads(json: JsValue): JsResult[LocalDate] =
      json.validate[String].map(LocalDate.parse)

    override def writes(o: LocalDate): JsValue = Json.toJson(o.toString)
  }

  implicit val instalmentFormat = Json.format[Instalment]
  implicit val scheduleFormat = Json.format[Schedule]
  implicit val addressFormat = Json.format[Address]
  implicit val debitFormat = Json.format[Debit]
  implicit val communicationPreferencesFormat = Json.format[CommunicationPreferences]
  implicit val selfAssessmentFormat = Json.format[SelfAssessment]
  implicit val taxPayerFormat = Json.format[Taxpayer]
  implicit val ttpArrangementFormat = Json.format[TTPArrangement]
}
