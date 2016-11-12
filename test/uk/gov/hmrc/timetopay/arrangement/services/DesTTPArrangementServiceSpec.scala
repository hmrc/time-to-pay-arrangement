package uk.gov.hmrc.timetopay.arrangement.services

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.scalatest.prop.TableDrivenPropertyChecks._
import uk.gov.hmrc.timetopay.arrangement.resources._

class DesTTPArrangementServiceSpec extends UnitSpec with WithFakeApplication with ScalaFutures {


  val desTTPArrangementService = new DesTTPArrangementService

  val taxPayerData = Table(
    ("taxPayer", "enforcementFlag", "message"),
    (scottishTaxpayer, Some("Summary Warrant"), "single scottish postcode"),
    (welshTaxPayer, Some("Distraint"), "single welsh postcode"),
    (englishTaxPayer, Some("Distraint"), "single english postcode"),
    (multipleScottishAddressTaxPayer, Some("Summary Warrant"), "multiple scottish postcode"),
    (multipleWelshAddressTaxPayer, Some("Distraint"), "multiple welsh postcode"),
    (multipleAddressTypeTaxPayer, None, "mixed postcodes"),
    (taxPayerWithNoAddress, None, "no addresss")
  )

  "DesTTPArrangementService " should {
    forAll(taxPayerData) { (taxpayer, enforcementFlag,message) =>
      s"return enforcementFlag =  $enforcementFlag for $message" in {
        val enforcementFlag = desTTPArrangementService.enforcementFlag(taxpayer)
        enforcementFlag shouldBe enforcementFlag
      }
    }
  }

}
