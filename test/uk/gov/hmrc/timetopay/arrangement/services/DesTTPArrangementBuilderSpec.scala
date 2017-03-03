/*
 * Copyright 2017 HM Revenue & Customs
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

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.timetopay.arrangement.TTPArrangement
import uk.gov.hmrc.timetopay.arrangement.config.{JurisdictionCheckerConfig, LetterAndControlAndJurisdictionChecker}
import uk.gov.hmrc.timetopay.arrangement.modelFormat._
import uk.gov.hmrc.timetopay.arrangement.resources.Taxpayers._
import uk.gov.hmrc.timetopay.arrangement.resources._
import org.mockito.Mockito._
class DesTTPArrangementBuilderSpec extends UnitSpec  with MockFactory  with ScalaFutures with OneAppPerSuite with MockitoSugar{

  val jurisdictionConfig = JurisdictionCheckerConfig("^(AB|DD|DG|EH|FK|G|HS|IV|KA|KW|KY|ML|PA|PH|TD|ZE)[0-9].*",
    "^(LL|SY|LD|HR|NP|CF|SA)[0-9].*")
   val  LetterAndControlConfigInject = MockitoSugar.mock[LetterAndControlAndJurisdictionChecker]
  when(LetterAndControlConfigInject.createJurisdictionCheckerConfig).thenReturn(new JurisdictionChecker(jurisdictionConfig))
  val desTTPArrangementService = new DesTTPArrangementBuilder(LetterAndControlConfigInject)

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
