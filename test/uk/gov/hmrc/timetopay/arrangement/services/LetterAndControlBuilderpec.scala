package uk.gov.hmrc.timetopay.arrangement.services

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.timetopay.arrangement.config.LetterAndControlConfig
import uk.gov.hmrc.timetopay.arrangement._
import uk.gov.hmrc.timetopay.arrangement.resources._
import uk.gov.hmrc.timetopay.arrangement.resources.Taxpayers._
import org.scalatest.prop.TableDrivenPropertyChecks._

class LetterAndControlBuilderpec extends UnitSpec with WithFakeApplication with ScalaFutures {

  val letterAndControlConfig = LetterAndControlConfig("XXXX", "XXXX","XXXX","XXXX","XXXX","XXXX","XXXX", "XXXX","XXXX")

  val letterAndControlService = new LetterAndControlBuilder(letterAndControlConfig)

  val taxPayerData = Table(
    ("taxPayer", "exceptionCode", "exceptionReason", "message"),
    (taxPayerWithEnglishAddress, None, None, "1 English Address"),
    (taxPayerWithWelshAddress, None, None, "1 Welsh Address"),
    (taxPayerWithNorthernIrelandAddress, None, None, "1 Northern Ireland Address"),
    (taxPayerWithMissingPostcodeAndLine1, Some("9"), Some("incomplete-address"), "Missing address line 1 and postcode"),
    (taxPayerWithMissingPostcode, Some("9"), Some("incomplete-address"), "missing postcode"),
    (taxPayerWithMultipleEnglishAddresses, None, None, "multiple English addresses"),
    (taxPayerWithEnglishAndScottishAddresses, Some("1"), Some("address-jurisdiction-conflict"), "an English and Scottish address"),
    (taxPayerWithEnglishAndForeignAddresses, None, None, "an English and Foreign address"),
    (taxPayerWithScottishAndForeignAddresses, Some("1"), Some("address-jurisdiction-conflict"), "a Scottish and Foreign address"),
    (taxPayerWithEnglishScottishAndForeignAddresses, Some("1"), Some("address-jurisdiction-conflict"), "an English, Scottish and Foreign address"),
    (taxPayerWithNoAddress, Some("8"), Some("no-address"), "no address"),
    (taxPayerWithLargePrintAndWelsh, Some("5"), Some("welsh-large-print-required"), "Welsh Language and Large Print")
  )

  "LetterAndControlService " should {
      forAll(taxPayerData) { (taxpayer, exceptionCode, exceptionReason, message) =>
        s"return (exceptionCode = $exceptionCode and exceptionReason = $exceptionReason) for $message" in {

        val result = letterAndControlService.create(TTPArrangement(None, None, "XXX", "XXX", taxpayer, schedule, None)).futureValue

        result.exceptionType shouldBe exceptionCode
        result.exceptionReason shouldBe exceptionReason
      }
    }
  }

}
