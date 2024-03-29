/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.timetopay.arrangement.services

import org.scalatest.prop.TableDrivenPropertyChecks._
import uk.gov.hmrc.timetopay.arrangement.support.{ITSpec, TestData}

import java.time.LocalDate.now
import uk.gov.hmrc.timetopay.arrangement.model.{Instalment, PaymentSchedule, TTPArrangement}

class LetterAndControlBuilderSpec extends ITSpec with TestData {
  private val letterAndControlBuilder = app.injector.instanceOf[LetterAndControlBuilder]

  import Taxpayers._

  private val taxPayerData = Table(
    ("taxPayer", "exceptionCode", "exceptionReason", "message"),
    (taxPayerWithEnglishAddress, None, None, "1 English Address"),
    (taxPayerWithEnglishAddressWithNoComsPref, None, None, "1 English Address and no comms preference"),
    (taxPayerWithWelshAddress, None, None, "1 Welsh Address"),
    (taxPayerWithNorthernIrelandAddress, None, None, "1 Northern Ireland Address"),
    (taxPayerWithMissingPostcodeAndLine1, Some("9"), Some("incomplete address"), "Missing address line 1 and missing postcode"),
    (taxPayerWithEmptyPostcodeAndMissingLine1, Some("9"), Some("incomplete address"), "Missing address line 1 and empty postcode"),
    (taxPayerWithMissingPostcode, Some("9"), Some("incomplete address"), "missing postcode"),
    (taxPayerWithEmptyPostcode, Some("9"), Some("incomplete address"), "empty postcode"),
    (taxPayerWithMissingLine1, Some("9"), Some("incomplete address"), "missing line 1"),
    (taxPayerWithMultipleEnglishAddresses, None, None, "multiple English addresses"),
    (taxPayerWithEnglishAndScottishAddresses, Some("1"), Some("address jurisdiction conflict"), "an English and Scottish address"),
    (taxPayerWithEnglishAndForeignAddresses, None, None, "an English and Foreign address"),
    (taxPayerWithScottishAndForeignAddresses, Some("1"), Some("address jurisdiction conflict"), "a Scottish and Foreign address"),
    (taxPayerWithEnglishScottishAndForeignAddresses, Some("1"), Some("address jurisdiction conflict"), "an English, Scottish and Foreign address"),
    (taxPayerWithNoAddress, Some("8"), Some("no address"), "no address"),
    (taxPayerWithLargePrintAndWelsh, Some("5"), Some("welsh large print required"), "Welsh Language and Large Print"))

  forAll(taxPayerData) { (taxpayer, exceptionCode, exceptionReason, message) =>
    s"LetterAndControlService should return (exceptionCode = $exceptionCode and exceptionReason = $exceptionReason) for $message" in {

      val result = letterAndControlBuilder.create(
        TTPArrangement(
          paymentPlanReference = "XXX",
          directDebitReference = "XXX",
          taxpayer             = taxpayer,
          bankDetails          = bankDetails,
          schedule             = schedule,
          desArrangement       = None))

      result.customerName shouldBe taxpayer.customerName
      result.salutation shouldBe s"Dear ${taxpayer.customerName}"
      result.exceptionType shouldBe exceptionCode
      result.exceptionReason shouldBe exceptionReason
    }
  }

  "LetterAndControlService should Format the clmPymtString correctly" in {
    val scheduleWithInstalments: PaymentSchedule = PaymentSchedule(now(), now(), 0.0, BigDecimal("100.98"), 100, 0.98, 100.98,
                                                                   List(
        Instalment(now(), 10.0),
        Instalment(now(), 10.0),
        Instalment(now(), 10.0),
        Instalment(now(), 10.0),
        Instalment(now(), 10.0),
        Instalment(now(), 10.0),
        Instalment(now(), 10.0),
        Instalment(now(), 10.0),
        Instalment(now(), 10.0),
        Instalment(now(), 10.98)))
    val result = letterAndControlBuilder.create(
      TTPArrangement(
        paymentPlanReference = "XXX",
        directDebitReference = "XXX",
        taxpayer             = taxpayer,
        bankDetails          = bankDetails,
        schedule             = scheduleWithInstalments,
        desArrangement       = None
      )
    )
    result.clmPymtString shouldBe "9 payments of £10.00 and final payment of £10.98"
    result.totalAll shouldBe "100.98"
  }

  "LetterAndControlService should Format the clmPymtString correctly for large numbers in" in {
    val scheduleWithInstalments: PaymentSchedule = PaymentSchedule(now(), now(), 5000000.0, BigDecimal("15000000.00"), 100, 0.00, 100.98,
                                                                   List(
        Instalment(now(), 100000000.00),
        Instalment(now(), 100000000.00),
        Instalment(now(), 100000000.00)))
    val result = letterAndControlBuilder.create(
      TTPArrangement(
        paymentPlanReference = "XXX",
        directDebitReference = "XXX",
        taxpayer             = taxpayer,
        bankDetails          = bankDetails,
        schedule             = scheduleWithInstalments,
        desArrangement       = None
      )
    )
    result.clmPymtString shouldBe "Initial payment of £5,000,000.00 then 2 payments of £100,000,000.00 and final payment of £100,000,000.00"
  }

  "LetterAndControlService should Format the clmPymtString correctly for a 2 month schedule with only a first and final payment" in {
    val scheduleWithInstalments =
      PaymentSchedule(now(), now(), 5000000.0, BigDecimal("15000000.00"), 100, 0.00, 100.98, List(Instalment(now(), 100000000.00), Instalment(now(), 100000000.00)))

    val result = letterAndControlBuilder.create(
      TTPArrangement(
        paymentPlanReference = "XXX",
        directDebitReference = "XXX",
        taxpayer             = taxpayer,
        bankDetails          = bankDetails,
        schedule             = scheduleWithInstalments,
        desArrangement       = None
      )
    )
    result.clmPymtString shouldBe "Initial payment of £5,000,000.00 then 1 payments of £100,000,000.00 and final payment of £100,000,000.00"
  }

}
