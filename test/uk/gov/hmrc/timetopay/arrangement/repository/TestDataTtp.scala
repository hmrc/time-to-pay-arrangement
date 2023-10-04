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

package uk.gov.hmrc.timetopay.arrangement.repository

import play.api.libs.json.Json
import uk.gov.hmrc.timetopay.arrangement.model.TTPArrangement

object TestDataTtp {

  lazy val auditTags = Map(
    "tag1" -> "value1",
    "tag2" -> "value2"
  )

  val arrangement: TTPArrangement = Json.parse(
    s"""
       |{
       |  "_id" : "XXX-XXX-XXX",
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
       |  "bankDetails": {
       |    "sortCode": "12-34-56",
       |    "accountNumber": "12345678",
       |    "accountName": "Mr John Campbell"
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

  //  val anonymisedArrangement: AnonymousTTPArrangement = Json.parse(
  //    """
  //       |{
  //       |  "_id" : "XXX-XXX-XXX",
  //       |  "createdOn" : {
  //       |    "$date" : {
  //       |      "$numberLong" : "1667304404080"
  //       |    }
  //       |  },
  //       |  "paymentPlanReference": "1234567890",
  //       |  "directDebitReference": "1234567890",
  //       |  "taxpayer": {
  //       |    "selfAssessment": {
  //       |      "utr": "1234567890"
  //       |    }
  //       |  },
  //       |  "bankDetails": {
  //       |    "sortCode": "12-34-56",
  //       |    "accountNumber": "12345678",
  //       |    "accountName": "Mr John Campbell"
  //       |  },
  //       |  "schedule": {
  //       |    "startDate": "2016-09-01",
  //       |    "endDate": "2017-08-01",
  //       |    "initialPayment": 50,
  //       |    "amountToPay": 5000,
  //       |    "instalmentBalance": 4950,
  //       |    "totalInterestCharged": 45.83,
  //       |    "totalPayable": 5045.83,
  //       |    "instalments": [
  //       |      {
  //       |        "paymentDate": "2016-10-01",
  //       |        "amount": 1248.95
  //       |      },
  //       |      {
  //       |        "paymentDate": "2016-11-01",
  //       |        "amount": 1248.95
  //       |      },
  //       |      {
  //       |        "paymentDate": "2016-12-01",
  //       |        "amount": 1248.95
  //       |      },
  //       |      {
  //       |        "paymentDate": "2017-01-01",
  //       |        "amount": 1248.95
  //       |      }
  //       |    ]
  //       |  },
  //       |  "desArrangement": {
  //       |    "ttpArrangement": {
  //       |      "startDate": "2016-08-09",
  //       |      "endDate": "2016-09-16",
  //       |      "firstPaymentDate": "2016-08-09",
  //       |      "firstPaymentAmount": "1248.95",
  //       |      "regularPaymentAmount": "1248.95",
  //       |      "regularPaymentFrequency": "Monthly",
  //       |      "reviewDate": "2016-08-09",
  //       |      "initials": "DOM",
  //       |      "enforcementAction": "Distraint",
  //       |      "directDebit": true,
  //       |      "debitDetails": [
  //       |        {
  //       |          "debitType": "IN2",
  //       |          "dueDate": "2004-07-31"
  //       |        }
  //       |      ],
  //       |      "saNote": "SA Note Text Here"
  //       |    }
  //       |  }
  //       |}""".stripMargin).as[AnonymousTTPArrangement]

}
