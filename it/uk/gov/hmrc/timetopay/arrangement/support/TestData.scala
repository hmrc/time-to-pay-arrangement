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

  //TODO Fill in valid addresss details
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

  //TODO Fill in valid addresss details
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

  //TODO Fill in valid addresss details
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

  //TODO Fill in valid addresss details
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

  //TODO Fill in valid addresss details
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

  //TODO Fill in valid addresss details
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
       |        },
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

  //TODO Fill in valid addresss details
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
       |        },
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

  //TODO Fill in valid addresss details
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
       |        },
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

  //TODO fill in valid address details
  val welshPreferenceJson =
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
       |        "welshLanguageIndicator": true,
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

  //TODO fill in valid address details
  val welshPreferenceLargePrintJson =
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
     |        "welshLanguageIndicator": true,
     |        "audioIndicator": false,
     |        "largePrintIndicator": true,
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

  //TODO Fill in valid addresss details
  val englishAudioIndicatorJson =
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
     |        "audioIndicator": true,
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

  //TODO Fill in valid addresss details
  val englishLargePrintJson =
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
     |        "largePrintIndicator": true,
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

  //TODO Fill in valid addresss details
  val englishBrailleJson =
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
     |        "brailleIndicator": true,
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
