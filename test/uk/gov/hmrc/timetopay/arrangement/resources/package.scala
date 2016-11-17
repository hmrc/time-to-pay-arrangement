package uk.gov.hmrc.timetopay.arrangement

import java.time.LocalDate

import play.api.libs.json.Json
import uk.gov.hmrc.timetopay.arrangement._
import uk.gov.hmrc.timetopay.arrangement.modelFormat._

import scala.io.Source

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
  val happyCommsPref = CommunicationPreferences(welshLanguageIndicator = false, audioIndicator = false, largePrintIndicator = false, brailleIndicator = false)
  val selfAssessment = SelfAssessment("XXX", Some(happyCommsPref), List())

  object Addresses {
    val englishAddress1 = Address(addressLine1 = "XXX", postCode = "B45 0HY")
    val englishAddress2 = Address(addressLine1 = "XXX", postCode = "B97 5HZ")
    val welshAddress = Address(addressLine1 = "XXX", postCode = "CF23 8PF")
    val northernIrelandAddress = Address(addressLine1 = "XXX", postCode = "BT52 2PP")
    val scottishAddress = Address(addressLine1 = "XXX", postCode = "G3 8NW")
    val foreignAddress = Address(addressLine1 = "XXX", postCode = "400089")
    val englishAddressMissingPostCodeAndLine1 = Address(addressLine1 = "", postCode = "")
    val englishAddressMissingPostCode = Address(addressLine1 = "XXXX", postCode = "")
    val scottishAddress1 = Address(addressLine1 = "XXX", addressLine2 = "XXX", addressLine3 = "XXX", addressLine4 = "XXXX", addressLine5 = "XXXX", postCode = "G3 8NW")
    val scottishAddress2 = Address(addressLine1 = "XXX", addressLine2 = "XXX", addressLine3 = "XXX", addressLine4 = "XXXX", addressLine5 = "XXXX", postCode = "EH14 8NW")
    val welshAddress1 = Address(addressLine1 = "XXX", addressLine2 = "XXX", addressLine3 = "XXX", addressLine4 = "XXXX", addressLine5 = "XXXX", postCode = "LL57 3DL")
    val welshAddress2 = Address(addressLine1 = "XXX", addressLine2 = "XXX", addressLine3 = "XXX", addressLine4 = "XXXX", addressLine5 = "XXXX", postCode = "SY23 3YA")
  }

  object Taxpayers {

    import Addresses._

    val taxPayerWithScottishAddress = Taxpayer("CustomerName", List(scottishAddress), selfAssessment)
    val taxPayerWithEnglishAddress = Taxpayer("CustomerName", List(englishAddress1), selfAssessment)
    val taxPayerWithWelshAddress = Taxpayer("CustomerName", List(welshAddress), selfAssessment)
    val taxPayerWithNorthernIrelandAddress = Taxpayer("CustomerName", List(northernIrelandAddress), selfAssessment)
    val taxPayerWithMissingPostcodeAndLine1 = Taxpayer("CustomerName", List(englishAddressMissingPostCodeAndLine1), selfAssessment)
    val taxPayerWithMissingPostcode = Taxpayer("CustomerName", List(englishAddressMissingPostCode), selfAssessment)
    val taxPayerWithMultipleEnglishAddresses = Taxpayer("CustomerName", List(englishAddress1, englishAddress2), selfAssessment)
    val taxPayerWithEnglishAndScottishAddresses = Taxpayer("CustomerName", List(englishAddress1, scottishAddress), selfAssessment)
    val taxPayerWithEnglishAndForeignAddresses = Taxpayer("CustomerName", List(englishAddress1, foreignAddress), selfAssessment)
    val taxPayerWithScottishAndForeignAddresses = Taxpayer("CustomerName", List(scottishAddress, foreignAddress), selfAssessment)
    val taxPayerWithEnglishScottishAndForeignAddresses = Taxpayer("CustomerName", List(englishAddress1, scottishAddress, foreignAddress), selfAssessment)
    val taxPayerWithNoAddress = Taxpayer("CustomerName", List(), selfAssessment)
    val unhappySelfAssessment = SelfAssessment("XXX", Some(happyCommsPref.copy(welshLanguageIndicator = true, largePrintIndicator = true)), null)
    val taxPayerWithLargePrintAndWelsh = Taxpayer("CustomerName", List(englishAddress1), unhappySelfAssessment)
    val taxPayerWithMultipleWelshAddresses = Taxpayer("CustomerName", List(welshAddress1, welshAddress2), selfAssessment)
    val taxPayerWithMultipleScottishAddresses = Taxpayer("CustomerName", List(scottishAddress1, scottishAddress2), selfAssessment)
    val taxPayerWithMultipleJurisdictions = Taxpayer("CustomerName", List(welshAddress, scottishAddress), selfAssessment)
  }

}
