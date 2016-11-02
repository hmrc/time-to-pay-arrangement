package uk.gov.hmrc.timetopay.arrangement.support

object TestData {

  //TODO Fill in valid addresss details

  val englishHappyJson =
    s"""
       |{
       |  "paymentPlanReference": "1234567890",
       |  "directDebitReference": "1234567890",
       |  "taxpayer": {
       |    "selfAssessment": {
       |      "utr": "1234567890",
       |      "addresses": [
       |        {
       |          "addressLine1": "",
       |          "addressLine2": "",
       |          "addressLine3": "",
       |          "addressLine4": "",
       |          "addressLine5": "",
       |          "postCode": ""
       |        }
       |      ],
       |      "communicationPreferences": {
       |        "welshLanguageIndicator": false,
       |        "audioIndicator": false,
       |        "largePrintIndicator": false,
       |        "brailleIndicator": false
       |      },
       |      "debits": [
       |        {
       |          "debitType": "IN2",
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
       |  }
       |}
    """.stripMargin


  val scottishHappyJson =
    s"""
       |{
       |  "paymentPlanReference": "1234567890",
       |  "directDebitReference": "1234567890",
       |  "taxpayer": {
       |    "selfAssessment": {
       |      "utr": "1234567890",
       |      "addresses": [
       |        {
       |          "addressLine1": "",
       |          "addressLine2": "",
       |          "addressLine3": "",
       |          "addressLine4": "",
       |          "addressLine5": "",
       |          "postCode": ""
       |        }
       |      ],
       |      "communicationPreferences": {
       |        "welshLanguageIndicator": false,
       |        "audioIndicator": false,
       |        "largePrintIndicator": false,
       |        "brailleIndicator": false
       |      },
       |      "debits": [
       |        {
       |          "debitType": "IN2",
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
       |  }
       |}
    """.stripMargin

  val welshHappyJson =
    s"""
       |{
       |  "paymentPlanReference": "1234567890",
       |  "directDebitReference": "1234567890",
       |  "taxpayer": {
       |    "selfAssessment": {
       |      "utr": "1234567890",
       |      "addresses": [
       |        {
       |          "addressLine1": "",
       |          "addressLine2": "",
       |          "addressLine3": "",
       |          "addressLine4": "",
       |          "addressLine5": "",
       |          "postCode": ""
       |        }
       |      ],
       |      "communicationPreferences": {
       |        "welshLanguageIndicator": false,
       |        "audioIndicator": false,
       |        "largePrintIndicator": false,
       |        "brailleIndicator": false
       |      },
       |      "debits": [
       |        {
       |          "debitType": "IN2",
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
       |  }
       |}
    """.stripMargin

  val englishBadAddressJson =
    s"""
       |{
       |  "paymentPlanReference": "1234567890",
       |  "directDebitReference": "1234567890",
       |  "taxpayer": {
       |    "selfAssessment": {
       |      "utr": "1234567890",
       |      "addresses": [
       |        {
       |          "addressLine1": "",
       |          "addressLine2": "",
       |          "addressLine3": "",
       |          "addressLine4": "",
       |          "addressLine5": "",
       |          "postCode": ""
       |        }
       |      ],
       |      "communicationPreferences": {
       |        "welshLanguageIndicator": false,
       |        "audioIndicator": false,
       |        "largePrintIndicator": false,
       |        "brailleIndicator": false
       |      },
       |      "debits": [
       |        {
       |          "debitType": "IN2",
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
       |  }
       |}
    """.stripMargin


  val scottishBadAddressJson =
    s"""
       |{
       |  "paymentPlanReference": "1234567890",
       |  "directDebitReference": "1234567890",
       |  "taxpayer": {
       |    "selfAssessment": {
       |      "utr": "1234567890",
       |      "addresses": [
       |        {
       |          "addressLine1": "",
       |          "addressLine2": "",
       |          "addressLine3": "",
       |          "addressLine4": "",
       |          "addressLine5": "",
       |          "postCode": ""
       |        }
       |      ],
       |      "communicationPreferences": {
       |        "welshLanguageIndicator": false,
       |        "audioIndicator": false,
       |        "largePrintIndicator": false,
       |        "brailleIndicator": false
       |      },
       |      "debits": [
       |        {
       |          "debitType": "IN2",
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
       |  }
       |}
    """.stripMargin

  val welshBadAddressJson =
    s"""
       |{
       |  "paymentPlanReference": "1234567890",
       |  "directDebitReference": "1234567890",
       |  "taxpayer": {
       |    "selfAssessment": {
       |      "utr": "1234567890",
       |      "addresses": [
       |        {
       |          "addressLine1": "",
       |          "addressLine2": "",
       |          "addressLine3": "",
       |          "addressLine4": "",
       |          "addressLine5": "",
       |          "postCode": ""
       |        }
       |      ],
       |      "communicationPreferences": {
       |        "welshLanguageIndicator": false,
       |        "audioIndicator": false,
       |        "largePrintIndicator": false,
       |        "brailleIndicator": false
       |      },
       |      "debits": [
       |        {
       |          "debitType": "IN2",
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
       |  }
       |}
    """.stripMargin

  val englishMultipleAddressJson =
    s"""
       |{
       |  "paymentPlanReference": "1234567890",
       |  "directDebitReference": "1234567890",
       |  "taxpayer": {
       |    "selfAssessment": {
       |      "utr": "1234567890",
       |      "addresses": [
       |        {
       |          "addressLine1": "",
       |          "addressLine2": "",
       |          "addressLine3": "",
       |          "addressLine4": "",
       |          "addressLine5": "",
       |          "postCode": ""
       |        }
       |      ],
       |      "communicationPreferences": {
       |        "welshLanguageIndicator": false,
       |        "audioIndicator": false,
       |        "largePrintIndicator": false,
       |        "brailleIndicator": false
       |      },
       |      "debits": [
       |        {
       |          "debitType": "IN2",
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
       |  }
       |}
    """.stripMargin


  val scottishMultipleAddressJson =
    s"""
       |{
       |  "paymentPlanReference": "1234567890",
       |  "directDebitReference": "1234567890",
       |  "taxpayer": {
       |    "selfAssessment": {
       |      "utr": "1234567890",
       |      "addresses": [
       |        {
       |          "addressLine1": "",
       |          "addressLine2": "",
       |          "addressLine3": "",
       |          "addressLine4": "",
       |          "addressLine5": "",
       |          "postCode": ""
       |        }
       |      ],
       |      "communicationPreferences": {
       |        "welshLanguageIndicator": false,
       |        "audioIndicator": false,
       |        "largePrintIndicator": false,
       |        "brailleIndicator": false
       |      },
       |      "debits": [
       |        {
       |          "debitType": "IN2",
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
       |  }
       |}
    """.stripMargin

  val welshMultipleAddressJson =
    s"""
       |{
       |  "paymentPlanReference": "1234567890",
       |  "directDebitReference": "1234567890",
       |  "taxpayer": {
       |    "selfAssessment": {
       |      "utr": "1234567890",
       |      "addresses": [
       |        {
       |          "addressLine1": "",
       |          "addressLine2": "",
       |          "addressLine3": "",
       |          "addressLine4": "",
       |          "addressLine5": "",
       |          "postCode": ""
       |        }
       |      ],
       |      "communicationPreferences": {
       |        "welshLanguageIndicator": false,
       |        "audioIndicator": false,
       |        "largePrintIndicator": false,
       |        "brailleIndicator": false
       |      },
       |      "debits": [
       |        {
       |          "debitType": "IN2",
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
       |  }
       |}
    """.stripMargin

  val englishNoAddressJson =
    s"""
       |{
       |  "paymentPlanReference": "1234567890",
       |  "directDebitReference": "1234567890",
       |  "taxpayer": {
       |    "selfAssessment": {
       |      "utr": "1234567890",
       |      "addresses": [
       |        {
       |          "addressLine1": "",
       |          "addressLine2": "",
       |          "addressLine3": "",
       |          "addressLine4": "",
       |          "addressLine5": "",
       |          "postCode": ""
       |        }
       |      ],
       |      "communicationPreferences": {
       |        "welshLanguageIndicator": false,
       |        "audioIndicator": false,
       |        "largePrintIndicator": false,
       |        "brailleIndicator": false
       |      },
       |      "debits": [
       |        {
       |          "debitType": "IN2",
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
       |  }
       |}
    """.stripMargin


  val scottishNoAddressJson =
    s"""
       |{
       |  "paymentPlanReference": "1234567890",
       |  "directDebitReference": "1234567890",
       |  "taxpayer": {
       |    "selfAssessment": {
       |      "utr": "1234567890",
       |      "addresses": [
       |        {
       |          "addressLine1": "",
       |          "addressLine2": "",
       |          "addressLine3": "",
       |          "addressLine4": "",
       |          "addressLine5": "",
       |          "postCode": ""
       |        }
       |      ],
       |      "communicationPreferences": {
       |        "welshLanguageIndicator": false,
       |        "audioIndicator": false,
       |        "largePrintIndicator": false,
       |        "brailleIndicator": false
       |      },
       |      "debits": [
       |        {
       |          "debitType": "IN2",
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
       |  }
       |}
    """.stripMargin

  val welshNoAddressJson =
    s"""
       |{
       |  "paymentPlanReference": "1234567890",
       |  "directDebitReference": "1234567890",
       |  "taxpayer": {
       |    "selfAssessment": {
       |      "utr": "1234567890",
       |      "addresses": [
       |        {
       |          "addressLine1": "",
       |          "addressLine2": "",
       |          "addressLine3": "",
       |          "addressLine4": "",
       |          "addressLine5": "",
       |          "postCode": ""
       |        }
       |      ],
       |      "communicationPreferences": {
       |        "welshLanguageIndicator": false,
       |        "audioIndicator": false,
       |        "largePrintIndicator": false,
       |        "brailleIndicator": false
       |      },
       |      "debits": [
       |        {
       |          "debitType": "IN2",
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
       |  }
       |}
    """.stripMargin
}
