package uk.gov.hmrc.timetopay.arrangement.services

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.timetopay.arrangement.models._
import uk.gov.hmrc.timetopay.arrangement.resources._

class LetterAndControlServiceSpec extends UnitSpec with WithFakeApplication with ScalaFutures {

  val letterAndControlService = LetterAndControlService

  "LetterAndControlService" should {
    "return no exception code when 1 English address" in {
      val selfAssessment = SelfAssessment("XXX", happyCommsPref, null)
      val taxPayer = Taxpayer("CustomerName", List(englishAddress1), selfAssessment)
      val ttpArrangement = TTPArrangement(None, None, "XXX", "XXX", taxPayer, schedule, None)
      val result = letterAndControlService.create(ttpArrangement).futureValue

      result.exceptionType shouldBe None
      result.exceptionReason shouldBe None
    }

    "return no exception code when 1 Welsh address" in {
      val selfAssessment = SelfAssessment("XXX", happyCommsPref, null)
      val taxPayer = Taxpayer("CustomerName", List(welshAddress), selfAssessment)
      val ttpArrangement = TTPArrangement(None, None, "XXX", "XXX", taxPayer, schedule, None)
      val result = letterAndControlService.create(ttpArrangement).futureValue

      result.exceptionType shouldBe None
      result.exceptionReason shouldBe None
    }

    "return no exception code when 1 Northern Ireland address" in {
      val selfAssessment = SelfAssessment("XXX", happyCommsPref, null)
      val taxPayer = Taxpayer("CustomerName", List(northernIrelandAddress), selfAssessment)
      val ttpArrangement = TTPArrangement(None, None, "XXX", "XXX", taxPayer, schedule, None)
      val result = letterAndControlService.create(ttpArrangement).futureValue

      result.exceptionType shouldBe None
      result.exceptionReason shouldBe None
    }

    "return exception code 9 and reason incomplete-address for missing address line 1 and postcode" in {
      val taxPayer = Taxpayer("CustomerName", List(englishAddressMissingPostCodeAndLine1), null)
      val ttpArrangement = TTPArrangement(None, None, "XXX", "XXX", taxPayer, schedule, None)
      val result = letterAndControlService.create(ttpArrangement).futureValue

      result.exceptionType.get shouldBe "9"
      result.exceptionReason.get shouldBe "incomplete-address"
    }

    "return exception code 9 and reason incomplete-address for missing postcode" in {
      val selfAssessment = SelfAssessment("XXX", happyCommsPref, null)
      val taxPayer = Taxpayer("CustomerName", List(englishAddressMissingPostCode), selfAssessment)
      val ttpArrangement = TTPArrangement(None, None, "XXX", "XXX", taxPayer, schedule, None)
      val result = letterAndControlService.create(ttpArrangement).futureValue

      result.exceptionType.get shouldBe "9"
      result.exceptionReason.get shouldBe "incomplete-address"
    }

    "return no exception code for multiple English addresses" in {
      val selfAssessment = SelfAssessment("XXX", happyCommsPref, null)
      val taxPayer = Taxpayer("CustomerName", List(englishAddress1, englishAddress2), selfAssessment)
      val ttpArrangement = TTPArrangement(None, None, "XXX", "XXX", taxPayer, schedule, None)
      val result = letterAndControlService.create(ttpArrangement)

      result.exceptionType shouldBe None
      result.exceptionReason shouldBe None
    }

    "return exception code 1 and reason address-jurisdiction-conflict for an English and Scottish address" in {
      val selfAssessment = SelfAssessment("XXX", happyCommsPref, null)
      val taxPayer = Taxpayer("CustomerName", List(englishAddress1, scottishAddress), selfAssessment)
      val ttpArrangement = TTPArrangement(None, None, "XXX", "XXX", taxPayer, schedule, None)
      val result = letterAndControlService.create(ttpArrangement).futureValue

      result.exceptionType.get shouldBe "1"
      result.exceptionReason.get shouldBe "address-jurisdiction-conflict"
    }

    "return no exception code and for an English and Foreign address" in {
      val selfAssessment = SelfAssessment("XXX", happyCommsPref, null)
      val taxPayer = Taxpayer("CustomerName", List(englishAddress1, foreignAddress), selfAssessment)
      val ttpArrangement = TTPArrangement(None, None, "XXX", "XXX", taxPayer, schedule, None)
      val result = letterAndControlService.create(ttpArrangement).futureValue

      result.exceptionType shouldBe None
      result.exceptionReason shouldBe None
    }

    "return exception code 1 and reason address-jurisdiction-conflict for a Scottish and Foreign address" in {
      val taxPayer = Taxpayer("CustomerName", List(scottishAddress, foreignAddress), null)
      val ttpArrangement = TTPArrangement(None, None, "XXX", "XXX", taxPayer, schedule, None)
      val result = letterAndControlService.create(ttpArrangement).futureValue

      result.exceptionType.get shouldBe "1"
      result.exceptionReason.get shouldBe "address-jurisdiction-conflict"
    }

    "return exception code 1 and reason address-jurisdiction-conflict for an English, Scottish and Foreign address" in {
      val taxPayer = Taxpayer("CustomerName", List(englishAddress1, scottishAddress, foreignAddress), null)
      val ttpArrangement = TTPArrangement(None, None, "XXX", "XXX", taxPayer, schedule, None)
      val result = letterAndControlService.create(ttpArrangement).futureValue

      result.exceptionType.get shouldBe "1"
      result.exceptionReason.get shouldBe "address-jurisdiction-conflict"
    }

    "return exception code 8 and reason no-address for no address" in {
      val taxPayer = Taxpayer("CustomerName", List(), null)
      val ttpArrangement = TTPArrangement(None, None, "XXX", "XXX", taxPayer, schedule, None)
      val result = letterAndControlService.create(ttpArrangement).futureValue

      result.exceptionType.get shouldBe "8"
      result.exceptionReason.get shouldBe "no-address"
    }

    "return exception code 5 and reason welsh-large-print-required for Welsh Language and Large Print" in {
      val unhappySelfAssessment = SelfAssessment("XXX", happyCommsPref.copy(welshLanguageIndicator = true, largePrintIndicator = true), null)
      val taxPayer = Taxpayer("CustomerName", List(englishAddress1), unhappySelfAssessment)
      val ttpArrangement = TTPArrangement(None, None, "XXX", "XXX", taxPayer, schedule, None)
      val result = letterAndControlService.create(ttpArrangement).futureValue

      result.exceptionType.get shouldBe "5"
      result.exceptionReason.get shouldBe "welsh-large-print-required"
    }

  }
}
