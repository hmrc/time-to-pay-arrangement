/*
 * Copyright 2019 HM Revenue & Customs
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

import org.scalatest.prop.TableDrivenPropertyChecks._
import uk.gov.hmrc.timetopay.arrangement.TTPArrangement
import uk.gov.hmrc.timetopay.arrangement.config.LetterAndControlAndJurisdictionChecker
import uk.gov.hmrc.timetopay.arrangement.modelFormat._
import uk.gov.hmrc.timetopay.arrangement.resources.Taxpayers._
import uk.gov.hmrc.timetopay.arrangement.resources._
import uk.gov.hmrc.timetopay.arrangement.support.ITSpec

class DesTTPArrangementBuilderSpec extends ITSpec {

  val LetterAndControlConfigInject = fakeApplication.injector.instanceOf[LetterAndControlAndJurisdictionChecker]
  val desTTPArrangementService = new DesTTPArrangementBuilder(LetterAndControlConfigInject, config)
  val taxPayerData = Table(
    ("taxPayer", "enforcementFlag", "message"),
    (taxPayerWithScottishAddress, "Summary Warrant", "single scottish postcode"),
    (taxPayerWithWelshAddress, "Distraint", "single welsh postcode"),
    (taxPayerWithEnglishAddress, "Distraint", "single english postcode"),
    (taxPayerWithMultipleScottishAddresses, "Summary Warrant", "multiple scottish postcode"),
    (taxPayerWithMultipleWelshAddresses, "Distraint", "multiple welsh postcode"),
    (taxPayerWithMultipleJurisdictions, "Other", "mixed postcodes"),
    (taxPayerWithNoAddress, "Other", "no addresss"))

  forAll(taxPayerData) { (taxpayer, enforcementFlag, message) =>
    s"DesTTPArrangementService should return enforcementFlag =  $enforcementFlag for $message  for $taxpayer" in {
      val flag = desTTPArrangementService.enforcementFlag(taxpayer)
      flag shouldBe enforcementFlag
    }
  }

  "DesTTPArrangementService create an des arrangement" in {
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
