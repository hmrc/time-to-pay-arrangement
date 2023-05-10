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

package uk.gov.hmrc.timetopay.arrangement.support

import java.time.{LocalDate, LocalDateTime}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.timetopay.arrangement.model.{Address, BankDetails, CommunicationPreferences, DesTTPArrangement, Instalment, LetterAndControl, PaymentSchedule, SelfAssessment, TTPArrangement, TTPArrangementWorkItem, Taxpayer}
import uk.gov.hmrc.timetopay.arrangement.model.modelFormat._

trait TestData {

  val ttpArrangement: TTPArrangement = Json.parse(
    s"""
       |{
       |  "paymentPlanReference": "12345678901234567890",
       |  "directDebitReference": "12345678901234567890",
       |  "taxpayer": {
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
       |    "customerName": "Customer Name",
       |    "selfAssessment": {
       |      "utr": "1234567890",
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
       |    "initialPayment": 50.00,
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
       |}
       |""".stripMargin).as[TTPArrangement]

  val ttparrangementRequest: JsValue = Json.parse(
    s"""
       |{
       |  "paymentPlanReference": "12345678901234567890",
       |  "directDebitReference": "12345678901234567890",
       |  "taxpayer": {
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
       |    "customerName": "Customer Name",
       |    "selfAssessment": {
       |      "utr": "1234567890",
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
       |    "initialPayment": 50.00,
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
       |  }
       |}""".stripMargin)

  val ttparrangementResponse: JsValue = Json.parse(
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
       |
       |}
       |""".stripMargin)

  val submitArrangementTTPArrangement: DesTTPArrangement =
    Json.parse(
      s"""
         |{
         |  "startDate": "2016-08-09",
         |  "endDate": "2016-09-16",
         |  "firstPaymentDate": "2016-08-09",
         |  "firstPaymentAmount": "90000.00",
         |  "regularPaymentAmount": "6000.00",
         |  "regularPaymentFrequency": "Monthly",
         |  "reviewDate": "2016-08-09",
         |  "initials": "ZZZ",
         |  "enforcementAction": "CCP",
         |  "directDebit": true,
         |  "debitDetails": [
         |    {
         |      "debitType": "IN2",
         |      "dueDate": "2004-07-31"
         |    }
         |  ],
         |  "saNote": "SA Note Text Here"
         |}""".stripMargin).as[DesTTPArrangement]

  val submitArrangementLetterAndControl: LetterAndControl =
    Json.parse(
      s"""{
         |  "customerName": "Customer Name",
         |  "salutation": "Dear Sir or Madam",
         |  "addressLine1": "Plaza 2",
         |  "addressLine2": "Ironmasters Way",
         |  "addressLine3": "Telford",
         |  "addressLine4": "Shropshire",
         |  "addressLine5": "UK",
         |  "postCode": "TF3 4NA",
         |  "totalAll": "50000",
         |  "clmIndicateInt": "Interest is due",
         |  "clmPymtString": "1 payment of x.xx then 11 payments of x.xx",
         |  "officeName1": "office name 1",
         |  "officeName2": "office name 2",
         |  "officePostcode": "TF2 8JU",
         |  "officePhone": "1234567",
         |  "officeFax": "12345678",
         |  "officeOpeningHours": "9-5",
         |  "template": "template",
         |  "exceptionType": "2",
         |  "exceptionReason": "Customer requires Large Format printing"
         |}
         |""".stripMargin).as[LetterAndControl]

  val taxpayer: Taxpayer =
    Json.parse(
      s"""{
         |  "customerName" : "Customer Name",
         |  "addresses": [
         |    {
         |      "addressLine1": "",
         |      "addressLine2": "",
         |      "addressLine3": "",
         |      "addressLine4": "",
         |      "addressLine5": "",
         |      "postcode": ""
         |    }
         |  ],
         |  "selfAssessment": {
         |    "utr": "1234567890",
         |    "communicationPreferences": {
         |      "welshLanguageIndicator": false,
         |      "audioIndicator": false,
         |      "largePrintIndicator": false,
         |      "brailleIndicator": false
         |    },
         |    "debits": [
         |      {
         |        "originCode": "IN2",
         |        "dueDate": "2004-07-31"
         |      }
         |    ]
         |  }
         |}""".stripMargin).as[Taxpayer]

  val bankDetails: BankDetails =
    Json.parse(
      s"""{
         |  "sortCode": "12-34-56",
         |  "accountNumber": "12345678",
         |  "accountName": "Mr John Campbell"
         |}""".stripMargin).as[BankDetails]

  val schedule: PaymentSchedule = PaymentSchedule(LocalDate.now(), LocalDate.now(), 0.0, BigDecimal("2000.00"), 0.0, 0.0, 0.0, List(Instalment(LocalDate.now(), 0.0)))
  val happyCommsPref = CommunicationPreferences(welshLanguageIndicator = false, audioIndicator = false, largePrintIndicator = false, brailleIndicator = false)
  val selfAssessment = SelfAssessment("XXX", Some(happyCommsPref), List())
  val selfAssessmentNoCommsPref = SelfAssessment("XXX", None, List())

  object Addresses {
    val englishAddress1 = Address(addressLine1 = "XXX", postcode = Some("B45 0HY"))
    val englishAddress2 = Address(addressLine1 = "XXX", postcode = Some("B97 5HZ"))
    val welshAddress = Address(addressLine1 = "XXX", postcode = Some("CF23 8PF"))
    val northernIrelandAddress = Address(addressLine1 = "XXX", postcode = Some("BT52 2PP"))
    val scottishAddress = Address(addressLine1 = "XXX", postcode = Some("G3 8NW"))
    val foreignAddress = Address(addressLine1 = "XXX", postcode = Some("400089"))
    val englishAddressMissingPostCodeAndLine1 = Address(addressLine1 = "", postcode = None)
    val englishAddressEmptyPostCodeAndMissingLine1 = Address(addressLine1 = "", postcode = Some(""))
    val englishAddressMissingPostCode = Address(addressLine1 = "XXXX", postcode = None)
    val englishAddressEmptyPostCode = Address(addressLine1 = "XXXX", postcode = Some(""))
    val englishAddressMissingLine1 = Address(addressLine1 = "", postcode = Some("XXXX"))
    val scottishAddress1 = Address(addressLine1 = "XXX", addressLine2 = Some("XXX"), addressLine3 = Some("XXX"), addressLine4 = Some("XXXX"), addressLine5 = Some("XXXX"), postcode = Some("G3 8NW"))
    val scottishAddress2 = Address(addressLine1 = "XXX", addressLine2 = Some("XXX"), addressLine3 = Some("XXX"), addressLine4 = Some("XXXX"), addressLine5 = Some("XXXX"), postcode = Some("EH14 8NW"))
    val welshAddress1 = Address(addressLine1 = "XXX", addressLine2 = Some("XXX"), addressLine3 = Some("XXX"), addressLine4 = Some("XXXX"), addressLine5 = Some("XXXX"), postcode = Some("LL57 3DL"))
    val welshAddress2 = Address(addressLine1 = "XXX", addressLine2 = Some("XXX"), addressLine3 = Some("XXX"), addressLine4 = Some("XXXX"), addressLine5 = Some("XXXX"), postcode = Some("SY23 3YA"))
  }

  object Taxpayers {

    import Addresses._

    val taxPayerWithEnglishAddressWithNoComsPref = Taxpayer("CustomerName", List(englishAddress1), selfAssessmentNoCommsPref)
    val taxPayerWithScottishAddress = Taxpayer("CustomerName", List(scottishAddress), selfAssessment)
    val taxPayerWithEnglishAddress = Taxpayer("CustomerName", List(englishAddress1), selfAssessment)
    val taxPayerWithWelshAddress = Taxpayer("CustomerName", List(welshAddress), selfAssessment)
    val taxPayerWithNorthernIrelandAddress = Taxpayer("CustomerName", List(northernIrelandAddress), selfAssessment)
    val taxPayerWithMissingPostcodeAndLine1 = Taxpayer("CustomerName", List(englishAddressMissingPostCodeAndLine1), selfAssessment)
    val taxPayerWithEmptyPostcodeAndMissingLine1 = Taxpayer("CustomerName", List(englishAddressEmptyPostCodeAndMissingLine1), selfAssessment)
    val taxPayerWithMissingLine1 = Taxpayer("CustomerName", List(englishAddressMissingLine1), selfAssessment)
    val taxPayerWithMissingPostcode = Taxpayer("CustomerName", List(englishAddressMissingPostCode), selfAssessment)
    val taxPayerWithEmptyPostcode = Taxpayer("CustomerName", List(englishAddressEmptyPostCode), selfAssessment)
    val taxPayerWithMultipleEnglishAddresses = Taxpayer("CustomerName", List(englishAddress1, englishAddress2), selfAssessment)
    val taxPayerWithEnglishAndScottishAddresses = Taxpayer("CustomerName", List(englishAddress1, scottishAddress), selfAssessment)
    val taxPayerWithEnglishAndForeignAddresses = Taxpayer("CustomerName", List(englishAddress1, foreignAddress), selfAssessment)
    val taxPayerWithScottishAndForeignAddresses = Taxpayer("CustomerName", List(scottishAddress, foreignAddress), selfAssessment)
    val taxPayerWithEnglishScottishAndForeignAddresses = Taxpayer("CustomerName", List(englishAddress1, scottishAddress, foreignAddress), selfAssessment)
    val taxPayerWithNoAddress = Taxpayer("CustomerName", List(), selfAssessment)
    val unhappySelfAssessment = SelfAssessment("XXX", Some(happyCommsPref.copy(welshLanguageIndicator = true, largePrintIndicator = true)), null)
    val taxPayerWithLargePrintAndWelsh = Taxpayer("CustomerName", List(englishAddress1), unhappySelfAssessment)
    val taxPayerWithMultipleWelshAddresses = Taxpayer("CustomerName", List(welshAddress1, welshAddress2), selfAssessment)
    val taxPayerWithMultipleScottishAddresses = Taxpayer("CustomerName", List(scottishAddress1, scottishAddress2), selfAssessment)
    val taxPayerWithMultipleJurisdictions = Taxpayer("CustomerName", List(welshAddress, scottishAddress), selfAssessment)

  }

  object AuditTags {
    val authToken = "authorization-value"
    val akamaiReputationValue = "akamai-reputation-value"
    val requestId = "request-id-value"
    val sessionId = "TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854"
    val trueClientIp = "client-ip"
    val trueClientPort = "client-port"
    val deviceId = "device-id"
    val rawSessionId: String = "TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854"

    val requestPath: String = "/fake-path"
  }
}
