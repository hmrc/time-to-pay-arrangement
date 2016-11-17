package uk.gov.hmrc.timetopay.arrangement.services

import java.time.LocalDate

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.scalatest.prop.TableDrivenPropertyChecks._
import uk.gov.hmrc.timetopay.arrangement.TTPArrangement
import uk.gov.hmrc.timetopay.arrangement.resources.Taxpayers._
import uk.gov.hmrc.timetopay.arrangement.resources._
import uk.gov.hmrc.timetopay.arrangement.modelFormat._

class DesTTPArrangementBuilderSpec extends UnitSpec with WithFakeApplication with ScalaFutures {


  val desTTPArrangementService = new DesTTPArrangementBuilder

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

    "create a des arrangement" in {
      implicit val arrangement = ttparrangementRequest.as[TTPArrangement]
      val desArrangement = desTTPArrangementService.create(arrangement).futureValue
      desArrangement.enforcementAction shouldBe "Distraint"
      desArrangement.directDebit shouldBe true
      desArrangement.initials shouldBe "ZZZ"
      desArrangement.startDate shouldBe LocalDate.parse("2016-09-01")
      desArrangement.regularPaymentFrequency shouldBe "Monthly"
      desArrangement.firstPaymentAmount shouldBe "1298.95"
      desArrangement.saNote shouldBe "SSTTP arrangement created online 1st payment amount of £1298.95 " +
        "1st payment due date 01/09/2016." +
        "Regular payment amount £1248.95. Regular payment frequency monthly. " +
        "Review date 22/08/2017 DDI Ref. 1234567890 PP Ref. 1234567890.TTP letter issued"
    }
  }
}
