/*
 * Copyright 2018 HM Revenue & Customs
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


import java.time.LocalDate

import org.mockito.Mockito.when
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.TableDrivenPropertyChecks._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.timetopay.arrangement._
import uk.gov.hmrc.timetopay.arrangement.config.{JurisdictionCheckerConfig, LetterAndControlAndJurisdictionChecker, LetterAndControlConfig}
import uk.gov.hmrc.timetopay.arrangement.resources.Taxpayers._
import uk.gov.hmrc.timetopay.arrangement.resources._

class LetterAndControlBuilderSpec extends UnitSpec with MockFactory   with ScalaFutures  with MockitoSugar{

  val letterAndControlConfig = LetterAndControlConfig("Dear", "XXXX","XXXX","XXXX","XXXX","XXXX","XXXX", "XXXX","XXXX")
  val  jurisdictionConfig = JurisdictionCheckerConfig("^(AB|DD|DG|EH|FK|G|HS|IV|KA|KW|KY|ML|PA|PH|TD|ZE)[0-9].*",
    "^(LL|SY|LD|HR|NP|CF|SA)[0-9].*")
  val  LetterAndControlConfigInject = MockitoSugar.mock[LetterAndControlAndJurisdictionChecker]

  when(LetterAndControlConfigInject.createLetterAndControlConfig).thenReturn(letterAndControlConfig)
  when(LetterAndControlConfigInject.createJurisdictionCheckerConfig).thenReturn(new JurisdictionChecker(jurisdictionConfig))
  val letterAndControlService = new LetterAndControlBuilder(LetterAndControlConfigInject)

  val taxPayerData = Table(
    ("taxPayer", "exceptionCode", "exceptionReason", "message"),
    (taxPayerWithEnglishAddress, None, None, "1 English Address"),
    (taxPayerWithEnglishAddressWithNoComsPref, None, None, "1 English Address and no comms preference"),
    (taxPayerWithWelshAddress, None, None, "1 Welsh Address"),
    (taxPayerWithNorthernIrelandAddress, None, None, "1 Northern Ireland Address"),
    (taxPayerWithMissingPostcodeAndLine1, Some("9"), Some("incomplete address"), "Missing address line 1 and postcode"),
    (taxPayerWithMissingPostcode, Some("9"), Some("incomplete address"), "missing line 1"),
    (taxPayerWithMissingLine1, Some("9"), Some("incomplete address"), "missing postcode"),
    (taxPayerWithMultipleEnglishAddresses, None, None, "multiple English addresses"),
    (taxPayerWithEnglishAndScottishAddresses, Some("1"), Some("address jurisdiction conflict"), "an English and Scottish address"),
    (taxPayerWithEnglishAndForeignAddresses, None, None, "an English and Foreign address"),
    (taxPayerWithScottishAndForeignAddresses, Some("1"), Some("address jurisdiction conflict"), "a Scottish and Foreign address"),
    (taxPayerWithEnglishScottishAndForeignAddresses, Some("1"), Some("address jurisdiction conflict"), "an English, Scottish and Foreign address"),
    (taxPayerWithNoAddress, Some("8"), Some("no address"), "no address"),
    (taxPayerWithLargePrintAndWelsh, Some("5"), Some("welsh large print required"), "Welsh Language and Large Print")
  )

  "LetterAndControlService " should {
      forAll(taxPayerData) { (taxpayer, exceptionCode, exceptionReason, message) =>
        s"return (exceptionCode = $exceptionCode and exceptionReason = $exceptionReason) for $message" in {

        val result = letterAndControlService.create(TTPArrangement(None, None, "XXX", "XXX", taxpayer, schedule, None))

        result.customerName shouldBe taxpayer.customerName
        result.salutation shouldBe s"Dear ${taxpayer.customerName}"
        result.exceptionType shouldBe exceptionCode
        result.exceptionReason shouldBe exceptionReason
      }
    }
    "Format the clmPymtString correctly" in {
      val scheduleWithInstalments: Schedule = Schedule(LocalDate.now(), LocalDate.now(), 0.0, BigDecimal("100.98"), 100, 0.98, 100.98,
        List(Instalment(LocalDate.now(), 10.0),
          Instalment(LocalDate.now(), 10.0),
          Instalment(LocalDate.now(), 10.0),
          Instalment(LocalDate.now(), 10.0),
          Instalment(LocalDate.now(), 10.0),
          Instalment(LocalDate.now(), 10.0),
          Instalment(LocalDate.now(), 10.0),
          Instalment(LocalDate.now(), 10.0),
          Instalment(LocalDate.now(), 10.0),
          Instalment(LocalDate.now(), 10.98)))
      val result = letterAndControlService.create(TTPArrangement(None, None, "XXX", "XXX", taxpayer, scheduleWithInstalments, None))
      result.clmPymtString shouldBe "Initial payment of £10.00 then 8 payments of £10.00 and final payment of £10.98"
      result.totalAll shouldBe "100.98"
    }
    "Format the clmPymtString correctly for large numbers in" in {
      val scheduleWithInstalments: Schedule = Schedule(LocalDate.now(), LocalDate.now(), 5000000.0, BigDecimal("15000000.00"), 100, 0.00, 100.98,
        List(Instalment(LocalDate.now(), 100000000.00),
          Instalment(LocalDate.now(), 100000000.00),
          Instalment(LocalDate.now(), 100000000.00)))
      val result = letterAndControlService.create(TTPArrangement(None, None, "XXX", "XXX", taxpayer, scheduleWithInstalments, None))
      result.clmPymtString shouldBe "Initial payment of £105,000,000.00 then 1 payments of £100,000,000.00 and final payment of £100,000,000.00"
    }
  }

}
