package uk.gov.hmrc.timetopay.arrangement.services

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.scalatest.prop.TableDrivenPropertyChecks._
import uk.gov.hmrc.timetopay.arrangement.resources.Taxpayers._

class DesTTPArrangementServiceSpec extends UnitSpec with WithFakeApplication with ScalaFutures {


  val desTTPArrangementService = new DesTTPArrangementService

  val taxPayerData = Table(
    ("taxPayer", "enforcementFlag", "message"),
    (taxPayerWithScottishAddress, Some("Summary Warrant"), "single scottish postcode"),
    (taxPayerWithWelshAddress, Some("Distraint"), "single welsh postcode"),
    (taxPayerWithEnglishAddress, Some("Distraint"), "single english postcode"),
    (taxPayerWithMultipleScottishAddresses, Some("Summary Warrant"), "multiple scottish postcode"),
    (taxPayerWithMultipleWelshAddresses, Some("Distraint"), "multiple welsh postcode"),
    (taxPayerWithMultipleJurisdictions, None, "mixed postcodes"),
    (taxPayerWithNoAddress, None, "no addresss")
  )

  "DesTTPArrangementService " should {
    forAll(taxPayerData) { (taxpayer, enforcementFlag,message) =>
      s"return enforcementFlag =  $enforcementFlag for $message" in {
        val flag = desTTPArrangementService.enforcementFlag(taxpayer)
        flag shouldBe enforcementFlag
      }
    }
  }
}
