# time-to-pay-arrangement

[![Build Status](https://travis-ci.org/hmrc/time-to-pay-arrangement.svg)](https://travis-ci.org/hmrc/time-to-pay-arrangement) [ ![Download](https://api.bintray.com/packages/hmrc/releases/time-to-pay-arrangement/images/download.svg) ](https://bintray.com/hmrc/releases/time-to-pay-arrangement/_latestVersion)

#### POST /ttparrangements

Sets up a new time to pay arrangement based on the arrangement submitted. 

Input
```
{
	"ttparrangement": {
		"paymentPlanReference": "1234567890",
		"directDebitReference": "1234567890",
		"taxpayer": {
		    "self-assessment" : {
		  	    "utr": "1234567890",
			    "debits": [{
				    "debitType": "IN2",
				    "dueDate": "2004-07-31"
			    }],
                "communicationPreferences": {
                    "welshLanguageIndicator": true,
                    "audioIndicator": false,
                    "largePrintIndicator": false,
                    "brailleIndicator": false
                }
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
			"instalments": [{
				"paymentDate": "2016-10-01",
				"amount": 1248.95
			}, {
				"paymentDate": "2016-11-01",
				"amount": 1248.95
			}, {
				"paymentDate": "2016-12-01",
				"amount": 1248.95
			}, {
				"paymentDate": "2017-01-01",
				"amount": 1248.95
			}]
		}
	}
}
```

| Status Code | Description |
|---|---|
| 201 | TTP Arrangement created with header location  |
| 401 | Not authorised to submit the TTP arrangement  |

#### GET /ttparrangements/{arrangement-identifier}

Returns a specific arrangement based on the identifier

```    
{
	"ttparrangement": {
	    "identifier" : "XXX-XXX-XXX-XXX",
	    "createdOn" : "2016-08-09",
		"paymentPlanReference": "1234567890",
		"directDebitReference": "1234567890",
		"taxpayer": {
            "self-assessment" : {
                "utr": "1234567890",
                "debits": [{
                    "debitType": "IN2",
                    "dueDate": "2004-07-31"
                }],
                "communicationPreferences": {
                    "welshLanguageIndicator": true,
                    "audioIndicator": false,
                    "largePrintIndicator": false,
                    "brailleIndicator": false
                }
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
			"instalments": [{
				"paymentDate": "2016-10-01",
				"amount": 1248.95
			}, {
				"paymentDate": "2016-11-01",
				"amount": 1248.95
			}, {
				"paymentDate": "2016-12-01",
				"amount": 1248.95
			}, {
				"paymentDate": "2017-01-01",
				"amount": 1248.95
			}]
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