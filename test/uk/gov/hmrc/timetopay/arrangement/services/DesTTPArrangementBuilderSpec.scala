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
    (taxPayerWithScottishAddress, "Summary Warrant", "single scottish postcode"),
    (taxPayerWithWelshAddress, "Distraint", "single welsh postcode"),
    (taxPayerWithEnglishAddress, "Distraint", "single english postcode"),
    (taxPayerWithMultipleScottishAddresses, "Summary Warrant", "multiple scottish postcode"),
    (taxPayerWithMultipleWelshAddresses, "Distraint", "multiple welsh postcode"),
    (taxPayerWithMultipleJurisdictions, "Other", "mixed postcodes"),
    (taxPayerWithNoAddress, "Other", "no addresss")
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
      val desArrangement = desTTPArrangementService.create(arrangement)
      desArrangement.enforcementAction shouldBe "Distraint"
      desArrangement.directDebit shouldBe true
      desArrangement.initials shouldBe "ZZZ"
      desArrangement.startDate shouldBe LocalDate.parse("2016-09-01")
      desArrangement.regularPaymentFrequency shouldBe "Monthly"
      desArrangement.firstPaymentAmount shouldBe "1298.95"
      val expectedResult: String = "DDI 12345678901234567890123456789012345678900123456, PP 12345678901234567890123456789012345678900123456, First Payment Due Date 01/10/2016, First Payment " +
        "£1298.95, Regular Payment £1248.95, " +
        "Frequency Monthly, " +
        "Final Payment £1248.95, Review Date 22/08"
      desArrangement.saNote.length shouldBe 250
      desArrangement.saNote shouldBe expectedResult
    }
  }
}
