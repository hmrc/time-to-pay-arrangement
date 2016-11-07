package uk.gov.hmrc.timetopay.arrangement.services

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.timetopay.arrangement.models.{Address, Taxpayer}

class DesTTPArrangementServiceSpec extends UnitSpec with WithFakeApplication with ScalaFutures {

  "DesTTPArrangementService" should {
    "return summary warrant for single scottish postcode" in {
      val scottishAddress = Address(addressLine1 = "XXX", addressLine2 = "XXX",addressLine3 = "XXX", addressLine4 = "XXXX", addressLine5 = "XXXX", postCode = "G3 8NW")
      val taxPayer = Taxpayer("CustomerName", List(scottishAddress), null)

      val enforcementFlag = DesTTPArrangementService.enforcementFlag(taxPayer)
      enforcementFlag.get shouldBe "Summary Warrant"

    }

    "return distraint for single welsh postcode" in {
      val welshAddress = Address(addressLine1 = "XXX", addressLine2 = "XXX",addressLine3 = "XXX", addressLine4 = "XXXX", addressLine5 = "XXXX", postCode = "CF23 8PF")
      val taxPayer = Taxpayer("CustomerName", List(welshAddress), null)

      val enforcementFlag = DesTTPArrangementService.enforcementFlag(taxPayer)
      enforcementFlag.get shouldBe "Distraint"

    }

    "return distraint for a single english postcode" in {
      val englishPostCode = Address(addressLine1 = "XXX", addressLine2 = "XXX",addressLine3 = "XXX", addressLine4 = "XXXX", addressLine5 = "XXXX", postCode = "B45 0HY")
      val taxPayer = Taxpayer("CustomerName", List(englishPostCode), null)

      val enforcementFlag = DesTTPArrangementService.enforcementFlag(taxPayer)
      enforcementFlag.get shouldBe "Distraint"

    }

    "return summary warrant for multiple scottish postcode" in {
      val scottishAddress1 = Address(addressLine1 = "XXX", addressLine2 = "XXX",addressLine3 = "XXX", addressLine4 = "XXXX", addressLine5 = "XXXX", postCode = "G3 8NW")
      val scottishAddress2 = Address(addressLine1 = "XXX", addressLine2 = "XXX",addressLine3 = "XXX", addressLine4 = "XXXX", addressLine5 = "XXXX", postCode = "EH14 8NW")

      val taxPayer = Taxpayer("CustomerName", List(scottishAddress1, scottishAddress2), null)

      val enforcementFlag = DesTTPArrangementService.enforcementFlag(taxPayer)
      enforcementFlag.get shouldBe "Summary Warrant"
    }

    "return distraint for multiple welsh postcode" in {
      val welshAddress1 = Address(addressLine1 = "XXX", addressLine2 = "XXX",addressLine3 = "XXX", addressLine4 = "XXXX", addressLine5 = "XXXX", postCode = "LL57 3DL")
      val welshAddress2 = Address(addressLine1 = "XXX", addressLine2 = "XXX",addressLine3 = "XXX", addressLine4 = "XXXX", addressLine5 = "XXXX", postCode = "SY23 3YA")

      val taxPayer = Taxpayer("CustomerName", List(welshAddress1, welshAddress2), null)

      val enforcementFlag = DesTTPArrangementService.enforcementFlag(taxPayer)
      enforcementFlag.get shouldBe "Distraint"

    }

    "return empty for mixed postcodes" in {
      val welshAddress = Address(addressLine1 = "XXX", addressLine2 = "XXX",addressLine3 = "XXX", addressLine4 = "XXXX", addressLine5 = "XXXX", postCode = "LL57 3DL")
      val scottishAddress = Address(addressLine1 = "XXX", addressLine2 = "XXX",addressLine3 = "XXX", addressLine4 = "XXXX", addressLine5 = "XXXX", postCode = "EH14 8NW")
      val taxPayer = Taxpayer("CustomerName", List(welshAddress, scottishAddress), null)

      val enforcementFlag = DesTTPArrangementService.enforcementFlag(taxPayer)
      enforcementFlag shouldBe None

    }

    "return empty enforcement flag for no address" in {
      val taxPayer = Taxpayer("CustomerName", List(), null)

      val enforcementFlag = DesTTPArrangementService.enforcementFlag(taxPayer)
      enforcementFlag shouldBe None

    }
  }

}
