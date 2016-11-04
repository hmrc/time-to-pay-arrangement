# time-to-pay-arrangement

[![Build Status](https://travis-ci.org/hmrc/time-to-pay-arrangement.svg)](https://travis-ci.org/hmrc/time-to-pay-arrangement) [ ![Download](https://api.bintray.com/packages/hmrc/releases/time-to-pay-arrangement/images/download.svg) ](https://bintray.com/hmrc/releases/time-to-pay-arrangement/_latestVersion)

#### POST /ttparrangements

Sets up a new time to pay arrangement based on the arrangement submitted. 

Input
```
{
  "paymentPlanReference": "1234567890",
  "directDebitReference": "1234567890",
  "taxpayer": {
   "customerName": "Customer name",
    "selfAssessment": {
      "utr": "1234567890",
      "addresses": [
        {
          "addressLine1": "",
          "addressLine2": "",
          "addressLine3": "",
          "addressLine4": "",
          "addressLine5": "",
          "postCode": ""
        }
      ],
      "communicationPreferences": {
        "welshLanguageIndicator": false,
        "audioIndicator": false,
        "largePrintIndicator": false,
        "brailleIndicator": false
      },
      "debits": [
        {
          "debitType": "IN2",
          "dueDate": "2004-07-31"
        }
      ]
    }
  },
  "schedule": {
    "startDate": "2016-09-01",
    "endDate": "2017-08-01",
    "initialPayment": 50,
    "amountToPay": 5000,
    "instalmentBalance": 4950,
    "totalInterestCharged": 45.83,
    "totalPayable": 5045.83,
    "instalments": [
      {
        "paymentDate": "2016-10-01",
        "amount": 1248.95
      },
      {
        "paymentDate": "2016-11-01",
        "amount": 1248.95
      },
      {
        "paymentDate": "2016-12-01",
        "amount": 1248.95
      },
      {
        "paymentDate": "2017-01-01",
        "amount": 1248.95
      }
    ]
  }
}
```

| Status Code | Description |
|---|---|
| 201 | TTP Arrangement created with header location  |
| 401 | Not authorised to submit the TTP arrangement  |

#### GET /ttparrangements/{arrangement-identifier}

Returns a specific arrangement based on the identifier. This api is for internal use only and the data lives for 30 days

```    
{
  "id" : "XXX-XXX-XXX",
  "createdOn" : "2016-08-09",
  "paymentPlanReference": "1234567890",
  "directDebitReference": "1234567890",
  "taxpayer": {
    "customerName": "Customer name",
    "selfAssessment": {
      "utr": "1234567890",
      "addresses": [
        {
          "addressLine1": "",
          "addressLine2": "",
          "addressLine3": "",
          "addressLine4": "",
          "addressLine5": "",
          "postCode": ""
        }
      ],
      "communicationPreferences": {
        "welshLanguageIndicator": false,
        "audioIndicator": false,
        "largePrintIndicator": false,
        "brailleIndicator": false
      },
      "debits": [
        {
          "debitType": "IN2",
          "dueDate": "2004-07-31"
        }
      ]
    }
  },
  "schedule": {
    "startDate": "2016-09-01",
    "endDate": "2017-08-01",
    "initialPayment": 50,
    "amountToPay": 5000,
    "instalmentBalance": 4950,
    "totalInterestCharged": 45.83,
    "totalPayable": 5045.83,
    "instalments": [
      {
        "paymentDate": "2016-10-01",
        "amount": 1248.95
      },
      {
        "paymentDate": "2016-11-01",
        "amount": 1248.95
      },
      {
        "paymentDate": "2016-12-01",
        "amount": 1248.95
      },
      {
        "paymentDate": "2017-01-01",
        "amount": 1248.95
      }
    ]
  },
  "desArrangement": {
    "ttpArrangement": {
      "startDate": "2016-08-09",
      "endDate": "2016-09-16",
      "firstPaymentDate": "2016-08-09",
      "firstPaymentAmount": "1248.95",
      "regularPaymentAmount": "1248.95",
      "regularPaymentFrequency": "Monthly",
      "reviewDate": "2016-08-09",
      "initials": "DOM",
      "enforcementAction": "Distraint",
      "directDebit": true,
      "debitDetails": [
        {
          "debitType": "IN2",
          "dueDate": "2004-07-31"
        }
      ],
      "saNote": "SA Note Text Here"
    },
    "letterAndControl": {
      "customerName": "Customer Name",
      "salutation": "Dear Sir or Madam",
      "addressLine1": "Plaza 2",
      "addressLine2": "Ironmasters Way",
      "addressLine3": "Telford",
      "addressLine4": "Shropshire",
      "addressLine5": "UK",
      "postCode": "TF3 4NA",
      "totalAll": "50000",
      "clmIndicateInt": "Interest is due",
      "clmPymtString": "Initial payment of 50 then 3 payments of 1248.95 and final payment of 1248.95",
      "officeName1": "office name 1",
      "officeName2": "office name 2",
      "officePostcode": "TF2 8JU",
      "officePhone": "1234567",
      "officeFax": "12345678",
      "officeOpeningHours": "9-5",
      "template": "template",
      "exceptionType": "2",
      "exceptionReason": "Customer requires Large Format printing"
    }
  }

}
```

| Status Code | Description |
|---|---|
| 200 | Returns TTP Arrangement  |
| 404 | Could not find the resource  |

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")