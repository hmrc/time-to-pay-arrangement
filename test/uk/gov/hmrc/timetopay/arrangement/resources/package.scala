package uk.gov.hmrc.timetopay.arrangement

import java.time.LocalDate

import play.api.libs.json.Json
import uk.gov.hmrc.timetopay.arrangement.models.{SelfAssessment, _}

import scala.io.Source
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._

package object resources {

  val ttparrangementRequest = Json.parse(Source.fromFile(s"test/uk/gov/hmrc/timetopay/arrangement/resources/TTPArrangementRequest.json").getLines.mkString)
  val ttparrangementResponse = Json.parse(Source.fromFile(s"test/uk/gov/hmrc/timetopay/arrangement/resources/TTPArrangementResponse.json").getLines.mkString)
  val submitArrangementTTPArrangement: DesTTPArrangement =
    Json.parse(Source.fromFile(s"test/uk/gov/hmrc/timetopay/arrangement/resources/DesTTPArrangement.json").getLines.mkString).as[DesTTPArrangement]
  val submitArrangementLetterAndControl: LetterAndControl =
    Json.parse(Source.fromFile(s"test/uk/gov/hmrc/timetopay/arrangement/resources/LetterAndControl.json").getLines.mkString).as[LetterAndControl]
  val taxpayer: Taxpayer =
    Json.parse(Source.fromFile(s"test/uk/gov/hmrc/timetopay/arrangement/resources/Taxpayer.json").getLines.mkString).as[Taxpayer]

  val schedule: Schedule = Schedule(LocalDate.now(), LocalDate.now(), 0.0, BigDecimal("2000.00"), 0.0, 0.0, 0.0, List(Instalment(LocalDate.now(), 0.0)))

  val englishAddress1 = Address(addressLine1 = "XXX", postCode = "B45 0HY")
  val englishAddress2 = Address(addressLine1 = "XXX", postCode = "B97 5HZ")
  val welshAddress = Address(addressLine1 = "XXX", postCode = "CF23 8PF")
  val northernIrelandAddress = Address(addressLine1 = "XXX", postCode = "BT52 2PP")
  val scottishAddress = Address(addressLine1 = "XXX", postCode = "G3 8NW")
  val foreignAddress = Address(addressLine1 = "XXX", postCode = "400089")
  val englishAddressMissingPostCodeAndLine1 = Address(addressLine1 = "", postCode = "")
  val englishAddressMissingPostCode = Address(addressLine1 = "XXXX", postCode = "")

  val happyCommsPref = CommunicationPreferences(false, false, false, false)
  val selfAssessment = SelfAssessment("XXX", happyCommsPref, null)

}
