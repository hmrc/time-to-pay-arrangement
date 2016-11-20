package uk.gov.hmrc.timetopay.arrangement.repositories


import org.scalatest.{MustMatchers, GivenWhenThen, BeforeAndAfter, FunSpec}
import play.api.libs.json.Json
import uk.gov.hmrc.mongo.MongoConnector
import uk.gov.hmrc.timetopay.arrangement.{TTPArrangementRepository, TTPArrangement}
import uk.gov.hmrc.timetopay.arrangement.modelFormat._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._


trait FutureHelpers {
  implicit class futureHelpers[T](f: Future[T]) {
    def waitFor(timeout: FiniteDuration = 10 seconds) = Await.result(f, timeout)
  }
}


class TTPArrangementRepositoryISpec extends FunSpec with BeforeAndAfter with GivenWhenThen with FutureHelpers with MustMatchers {

  private def databaseName = "test-" + this.getClass.getSimpleName
  private def mongoUri: String = s"mongodb://127.0.0.1:27017/$databaseName"
  implicit val mongoConnectorForTest = new MongoConnector(mongoUri)
  implicit val mongo = mongoConnectorForTest.db

  val repository =  new TTPArrangementRepository

  before {
    clear()
  }

  after {
    clear()
  }

  def clear(): Unit = {
   repository.removeAll().waitFor()
  }

  val arrangement = Json.parse(s"""
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
              |        "postCode": ""
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

  it("should add save a TTPArrangement") {

    val result = repository.save(arrangement).waitFor()

    result.get.taxpayer.customerName mustBe arrangement.taxpayer.customerName

  }

  it("should get a TTPArrangement for given id") {
    repository.save(arrangement).waitFor()

    val loaded = repository.findById(arrangement.id.get).waitFor().get
    loaded.id.get mustBe arrangement.id.get

  }

}
