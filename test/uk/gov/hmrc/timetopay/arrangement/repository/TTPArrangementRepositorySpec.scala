/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.timetopay.arrangement.repository

import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.timetopay.arrangement.TTPArrangement
import uk.gov.hmrc.timetopay.arrangement.modelFormat._
import uk.gov.hmrc.timetopay.arrangement.support.ITSpec

class TTPArrangementRepositorySpec extends ITSpec {
  val logger: Logger = Logger(getClass)

  private val arrangementRepo = fakeApplication.injector.instanceOf[TTPArrangementRepository]
  private val arrangement = Json.parse(
    s"""
       |{
       |  "id" : "XXX-XXX-XXX",
       |  "createdOn" : "2016-11-07T18:15:57.581",
       |  "paymentPlanReference": "1234567890",
       |  "directDebitReference": "1234567890",
       |  "taxpayer": {
       |    "customerName" : "Customer Name",
       |    "addresses": [
       |      {
       |        "addressLine1": "",
       |        "addressLine2": "",
       |        "addressLine3": "",
       |        "addressLine4": "",
       |        "addressLine5": "",
       |        "postcode": ""
       |      }
       |    ],
       |    "selfAssessment": {
       |      "utr": "1234567890",
       |
              |      "communicationPreferences": {
       |        "welshLanguageIndicator": false,
       |        "audioIndicator": false,
       |        "largePrintIndicator": false,
       |        "brailleIndicator": false
       |      },
       |      "debits": [
       |        {
       |          "originCode": "IN2",
       |          "dueDate": "2004-07-31"
       |        }
       |      ]
       |    }
       |  },
       |  "schedule": {
       |    "startDate": "2016-09-01",
       |    "endDate": "2017-08-01",
       |    "initialPayment": 50,
       |    "amountToPay": 5000,
       |    "instalmentBalance": 4950,
       |    "totalInterestCharged": 45.83,
       |    "totalPayable": 5045.83,
       |    "instalments": [
       |      {
       |        "paymentDate": "2016-10-01",
       |        "amount": 1248.95
       |      },
       |      {
       |        "paymentDate": "2016-11-01",
       |        "amount": 1248.95
       |      },
       |      {
       |        "paymentDate": "2016-12-01",
       |        "amount": 1248.95
       |      },
       |      {
       |        "paymentDate": "2017-01-01",
       |        "amount": 1248.95
       |      }
       |    ]
       |  },
       |  "desArrangement": {
       |    "ttpArrangement": {
       |      "startDate": "2016-08-09",
       |      "endDate": "2016-09-16",
       |      "firstPaymentDate": "2016-08-09",
       |      "firstPaymentAmount": "1248.95",
       |      "regularPaymentAmount": "1248.95",
       |      "regularPaymentFrequency": "Monthly",
       |      "reviewDate": "2016-08-09",
       |      "initials": "DOM",
       |      "enforcementAction": "Distraint",
       |      "directDebit": true,
       |      "debitDetails": [
       |        {
       |          "debitType": "IN2",
       |          "dueDate": "2004-07-31"
       |        }
       |      ],
       |      "saNote": "SA Note Text Here"
       |    },
       |    "letterAndControl": {
       |      "customerName": "Customer Name",
       |      "salutation": "Dear Sir or Madam",
       |      "addressLine1": "Plaza 2",
       |      "addressLine2": "Ironmasters Way",
       |      "addressLine3": "Telford",
       |      "addressLine4": "Shropshire",
       |      "addressLine5": "UK",
       |      "postCode": "TF3 4NA",
       |      "totalAll": "50000",
       |      "clmIndicateInt": "Interest is due",
       |      "clmPymtString": "Initial payment of 50 then 3 payments of 1248.95 and final payment of 1248.95",
       |      "officeName1": "office name 1",
       |      "officeName2": "office name 2",
       |      "officePostcode": "TF2 8JU",
       |      "officePhone": "1234567",
       |      "officeFax": "12345678",
       |      "officeOpeningHours": "9-5",
       |      "template": "template",
       |      "exceptionType": "2",
       |      "exceptionReason": "Customer requires Large Format printing"
       |    }
       |  }
       |}""".stripMargin).as[TTPArrangement]

  override def beforeEach(): Unit = {
    arrangementRepo.collection.drop(false).futureValue
    ()
  }

  override def afterEach(): Unit = {
    arrangementRepo.collection.drop(false).futureValue
    ()
  }

  "should add save a TTPArrangement" in {

    val result = arrangementRepo.doInsert(arrangement).futureValue
    result.get.taxpayer.selfAssessment.utr shouldBe arrangement.taxpayer.selfAssessment.utr

  }

  "should get a TTPArrangement for given id" in {

    logger.warn(arrangement.toString)
    arrangementRepo.doInsert(arrangement).futureValue

    val loaded = arrangementRepo.findByIdLocal(arrangement.id.get).futureValue.get
    assert(loaded.toString.contains("desArrangement"))
    assert(loaded.toString.contains("XXX-XXX-XXX"))
  }

  "should not save any personal data in" in {
    arrangementRepo.doInsert(arrangement).futureValue

    val loaded = arrangementRepo.findByIdLocal(arrangement.id.get).futureValue.get
    assert(!loaded.toString.contains("Customer Name"))
    assert(!loaded.toString.contains("addresses"))
  }
}
