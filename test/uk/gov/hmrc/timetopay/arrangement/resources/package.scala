/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.timetopay.arrangement

import java.time.LocalDate

import play.api.libs.json.Json
import timetopayarrangement.SaUtr
import timetopayarrangement.des.model.{DesTtpArrangement, LetterAndControl}

package object resources {

  val setupArrangementRequet = Json.parse(s"""
                                           |{
                                           |  "paymentPlanReference": "12345678901234567890123456789012345678900123456",
                                           |  "directDebitReference": "12345678901234567890123456789012345678900123456",
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
                                           |  "schedule": {
                                           |    "startDate": "2016-09-01",
                                           |    "endDate": "2017-08-01",
                                           |    "initialPayment": 50,
                                           |    "amountToPay": 50000000,
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



  val submitArrangementTTPArrangement: DesTtpArrangement =
    Json.parse(s"""
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
                 |}""".stripMargin).as[DesTtpArrangement]

  val letterAndControl: LetterAndControl =
    Json.parse(s"""{
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

  val taxpayer: TaxpayerDetails =
    Json.parse(s"""{
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
                 |}""".stripMargin).as[TaxpayerDetails]

  val schedule: Schedule = Schedule(LocalDate.now(), LocalDate.now(), 0.0, BigDecimal("2000.00"), 0.0, 0.0, 0.0, List(Instalment(LocalDate.now(), 0.0)))
  val happyCommsPref = CommunicationPreferences(welshLanguageIndicator = false, audioIndicator = false, largePrintIndicator = false, brailleIndicator = false)
  val selfAssessment = SelfAssessment(SaUtr("XXX"), Some(happyCommsPref), List())
  val selfAssessmentNoCommsPref = SelfAssessment(SaUtr("XXX"), None, List())

  object Addresses {
    val englishAddress1 = Address(addressLine1 = "XXX", postcode = "B45 0HY")
    val englishAddress2 = Address(addressLine1 = "XXX", postcode = "B97 5HZ")
    val welshAddress = Address(addressLine1 = "XXX", postcode = "CF23 8PF")
    val northernIrelandAddress = Address(addressLine1 = "XXX", postcode = "BT52 2PP")
    val scottishAddress = Address(addressLine1 = "XXX", postcode = "G3 8NW")
    val foreignAddress = Address(addressLine1 = "XXX", postcode = "400089")
    val englishAddressMissingPostCodeAndLine1 = Address(addressLine1 = "", postcode = "")
    val englishAddressMissingPostCode = Address(addressLine1 = "XXXX", postcode = "")
    val englishAddressMissingLine1= Address(addressLine1 = "", postcode = "XXXX")
    val scottishAddress1 = Address(addressLine1 = "XXX", addressLine2 = Some("XXX"), addressLine3 = Some("XXX"), addressLine4 = Some("XXXX"), addressLine5 = Some("XXXX"), postcode = "G3 8NW")
    val scottishAddress2 = Address(addressLine1 = "XXX", addressLine2 = Some("XXX"), addressLine3 = Some("XXX"), addressLine4 = Some("XXXX"), addressLine5 = Some("XXXX"), postcode = "EH14 8NW")
    val welshAddress1 = Address(addressLine1 = "XXX", addressLine2 = Some("XXX"), addressLine3 = Some("XXX"), addressLine4 = Some("XXXX"), addressLine5 = Some("XXXX"), postcode = "LL57 3DL")
    val welshAddress2 = Address(addressLine1 = "XXX", addressLine2 = Some("XXX"), addressLine3 = Some("XXX"), addressLine4 = Some("XXXX"), addressLine5 = Some("XXXX"), postcode = "SY23 3YA")
  }

  object Taxpayers {

    import Addresses._

    val taxPayerWithEnglishAddressWithNoComsPref = TaxpayerDetails("CustomerName", List(englishAddress1), selfAssessmentNoCommsPref)
    val taxPayerWithScottishAddress = TaxpayerDetails("CustomerName", List(scottishAddress), selfAssessment)
    val taxPayerWithEnglishAddress = TaxpayerDetails("CustomerName", List(englishAddress1), selfAssessment)
    val taxPayerWithWelshAddress = TaxpayerDetails("CustomerName", List(welshAddress), selfAssessment)
    val taxPayerWithNorthernIrelandAddress = TaxpayerDetails("CustomerName", List(northernIrelandAddress), selfAssessment)
    val taxPayerWithMissingPostcodeAndLine1 = TaxpayerDetails("CustomerName", List(englishAddressMissingPostCodeAndLine1), selfAssessment)
    val taxPayerWithMissingLine1 =  TaxpayerDetails("CustomerName", List(englishAddressMissingLine1), selfAssessment)
    val taxPayerWithMissingPostcode = TaxpayerDetails("CustomerName", List(englishAddressMissingPostCode), selfAssessment)
    val taxPayerWithMultipleEnglishAddresses = TaxpayerDetails("CustomerName", List(englishAddress1, englishAddress2), selfAssessment)
    val taxPayerWithEnglishAndScottishAddresses = TaxpayerDetails("CustomerName", List(englishAddress1, scottishAddress), selfAssessment)
    val taxPayerWithEnglishAndForeignAddresses = TaxpayerDetails("CustomerName", List(englishAddress1, foreignAddress), selfAssessment)
    val taxPayerWithScottishAndForeignAddresses = TaxpayerDetails("CustomerName", List(scottishAddress, foreignAddress), selfAssessment)
    val taxPayerWithEnglishScottishAndForeignAddresses = TaxpayerDetails("CustomerName", List(englishAddress1, scottishAddress, foreignAddress), selfAssessment)
    val taxPayerWithNoAddress = TaxpayerDetails("CustomerName", List(), selfAssessment)
    val unhappySelfAssessment = SelfAssessment(SaUtr("XXX"), Some(happyCommsPref.copy(welshLanguageIndicator = true, largePrintIndicator = true)), null)
    val taxPayerWithLargePrintAndWelsh = TaxpayerDetails("CustomerName", List(englishAddress1), unhappySelfAssessment)
    val taxPayerWithMultipleWelshAddresses = TaxpayerDetails("CustomerName", List(welshAddress1, welshAddress2), selfAssessment)
    val taxPayerWithMultipleScottishAddresses = TaxpayerDetails("CustomerName", List(scottishAddress1, scottishAddress2), selfAssessment)
    val taxPayerWithMultipleJurisdictions = TaxpayerDetails("CustomerName", List(welshAddress, scottishAddress), selfAssessment)

  }

}
