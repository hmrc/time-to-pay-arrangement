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

import java.time.LocalDate
import uk.gov.hmrc.timetopay.arrangement.model.TTPArrangement
import uk.gov.hmrc.timetopay.arrangement.support.{ITSpec, TestData}

class DesTTPArrangementBuilderSpec extends ITSpec with TestData {

  import Taxpayers._

  private val desTTPArrangementService = new DesTTPArrangementBuilder(config)
  private val taxPayerData = Table(
    ("taxPayer", "enforcementFlag", "message"),
    (taxPayerWithScottishAddress, "Summary Warrant", "single scottish postcode"),
    (taxPayerWithWelshAddress, "Distraint", "single welsh postcode"),
    (taxPayerWithEnglishAddress, "Distraint", "single english postcode"),
    (taxPayerWithMultipleScottishAddresses, "Summary Warrant", "multiple scottish postcode"),
    (taxPayerWithMultipleWelshAddresses, "Distraint", "multiple welsh postcode"),
    (taxPayerWithMultipleJurisdictions, "Other", "mixed postcodes"),
    (taxPayerWithEmptyPostcode, "Other", "empty postcode"),
    (taxPayerWithMissingPostcode, "Other", "missing postcode"),
    (taxPayerWithNoAddress, "Other", "no addresss")
  )

  forAll(taxPayerData) { (taxpayer, enforcementFlag, message) =>
    s"DesTTPArrangementService should return enforcementFlag =  $enforcementFlag for $message  for $taxpayer" in {
      val flag = desTTPArrangementService.enforcementFlag(taxpayer)
      flag shouldBe enforcementFlag
    }
  }

  "DesTTPArrangementService create an des arrangement" in {
    val arrangement: TTPArrangement = ttparrangementRequest.as[TTPArrangement].copy(taxpayer = taxPayerWithEnglishAddress)
    val desArrangement = desTTPArrangementService.create(arrangement)
    desArrangement.enforcementAction shouldBe "Distraint"
    desArrangement.directDebit shouldBe true
    desArrangement.initials shouldBe "ZZZ"
    desArrangement.startDate shouldBe LocalDate.parse("2016-09-01")
    desArrangement.regularPaymentFrequency shouldBe "Monthly"
    desArrangement.firstPaymentAmount shouldBe "1248.95"
    desArrangement.endDate shouldBe LocalDate.parse("2017-08-01")
    val expectedResult: String = "DDI 12345678901234567890, PP 12345678901234567890, " +
      "initial payment of £50.00 on 01/09/2016, " +
      "first regular payment of £1248.95 " +
      "from 01/10/2016, frequency monthly, " +
      "final payment of £1248.95 on 01/08/2017, " +
      "review date 22/08/2017"
    desArrangement.saNote.length should be <= 250
    desArrangement.saNote shouldBe expectedResult
  }

}
