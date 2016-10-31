# time-to-pay-arrangement

[![Build Status](https://travis-ci.org/hmrc/time-to-pay-arrangement.svg)](https://travis-ci.org/hmrc/time-to-pay-arrangement) [ ![Download](https://api.bintray.com/packages/hmrc/releases/time-to-pay-arrangement/images/download.svg) ](https://bintray.com/hmrc/releases/time-to-pay-arrangement/_latestVersion)

#### GET /arrangements?utr={utr}&ddr={ddr}&ppr={ppr}

Requires Unique Tax Reference, Direct Debit Reference and Payment Plan Reference. 
Returns a time to pay arrangement that can be submitted:
```
{
	"arrangement": {
		"startDate": "2016-08-09",
		"endDate": "2016-09-16",
		"firstPaymentDate": "2016-08-09",
		"firstPaymentAmount": "90000.00",
		"regularPaymentAmount": "6000.00",
		"regularPaymentFrequency": "Monthly",
		"reviewDate": "2016-08-09",
		"initials": "DOM",
		"enforcementAction": "CCP",
		"directDebit": true,
		"debitDetails": [{
			"debitType": "IN2",
			"dueDate": "2004-07-31"
		}],
		"utr" : "1234567890",
		"paymentPlanReference": "1234567890",
		"directDebitReference": "1234567890"
	}
}
```

#### POST /ttparrangements

Sets up a new time to pay arrangement based on the arrangement submitted. 

Input/Output

HeaderLocation xxxxx/arrangements/{arrangement-identifier}

#### GET /ttparrangements?utr={utr}

Returns a list of time to pay arrangements set up for given UTR

#### GET /ttparrangements/{arrangement-identifier}

Returns a specific arrangement based on the identifier

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")