
package uk.gov.hmrc.timetopay.arrangement.services

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.{AuditChannel, AuditConnector, AuditResult, DatastreamMetrics}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import uk.gov.hmrc.timetopay.arrangement.connectors.SubmissionError
import uk.gov.hmrc.timetopay.arrangement.support.{ITSpec, TestData}

import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends ITSpec with TestData {

  def testChecks(details: JsValue)(event: ExtendedDataEvent): Any = {
    event.auditSource shouldEqual "pay-what-you-owe"
    event.detail shouldEqual Json.toJson(details)
  }

  "audit event for sendSubmissionSucceededEvent" in {
    val expected: JsValue =
      Json.parse(
        s"""{
           |  "status":"successfully submitted direct debit and TTP Arrangement",
           |  "utr":"1234567890",
           |  "bankDetails": {
           |    "name": "Mr John Campbell",
           |    "accountNumber": "12345678",
           |    "sortCode": "12-34-56"
           |  },
           |  "schedule": {
           |    "initialPaymentAmount":50,
           |    "installments":[
           |      {
           |        "paymentDate":"2016-10-01",
           |        "amount":1248.95
           |      },
           |      {
           |        "paymentDate":"2016-11-01",
           |        "amount":1248.95
           |      },
           |      {
           |        "paymentDate":"2016-12-01",
           |        "amount":1248.95
           |      },
           |      {
           |        "paymentDate":"2017-01-01",
           |        "amount":1248.95
           |       }
           |     ],
           |     "numberOfInstallments":4,
           |     "installmentLengthCalendarMonths":11,
           |     "totalPaymentWithoutInterest":5000,
           |     "totalInterestCharged":45.83,
           |     "totalPayable":5045.83
           |   }
           |}
           |""".stripMargin)

    val auditService = new AuditService(StubAuditConnector(expected)(testChecks))
    auditService.sendSubmissionSucceededEvent(taxpayer, bankDetails, paymentSchedule, auditTags)
  }

  "audit event for sendArrangementSubmissionFailedEvent" in {
    val submissionError = SubmissionError(999, "error-message")
    val expected: JsValue =
      Json.parse(
        s"""{
           |  "status":"submitted direct debit but failed to submit TTP Arrangement",
           |  "submissionError":{
           |    "code":999,
           |    "message":"error-message"
           |  },
           |  "utr":"1234567890",
           |  "bankDetails": {
           |    "name": "Mr John Campbell",
           |    "accountNumber": "12345678",
           |    "sortCode": "12-34-56"
           |  },
           |  "schedule": {
           |    "initialPaymentAmount":50,
           |    "installments":[
           |      {
           |        "paymentDate":"2016-10-01",
           |        "amount":1248.95
           |      },
           |      {
           |        "paymentDate":"2016-11-01",
           |        "amount":1248.95
           |      },
           |      {
           |        "paymentDate":"2016-12-01",
           |        "amount":1248.95
           |      },
           |      {
           |        "paymentDate":"2017-01-01",
           |        "amount":1248.95
           |       }
           |     ],
           |     "numberOfInstallments":4,
           |     "installmentLengthCalendarMonths":11,
           |     "totalPaymentWithoutInterest":5000,
           |     "totalInterestCharged":45.83,
           |     "totalPayable":5045.83
           |   }
           |}
           |""".stripMargin)

    val auditService = new AuditService(StubAuditConnector(expected)(testChecks))
    auditService.sendArrangementSubmissionFailedEvent(taxpayer, bankDetails, paymentSchedule, submissionError, auditTags)
  }


  case class StubAuditConnector(details: JsValue)(callback: JsValue => ExtendedDataEvent => Any) extends AuditConnector {
    override def auditingConfig: AuditingConfig = ???

    override def auditChannel: AuditChannel = ???

    override def datastreamMetrics: DatastreamMetrics = ???

    override def sendExtendedEvent(event: ExtendedDataEvent)(implicit hc: HeaderCarrier = HeaderCarrier(), ec: ExecutionContext): Future[AuditResult] = {
      logger.info(">>>sendEvent CALLED " + event)
      callback(details)(event)
      Future.successful(AuditResult.Success)
    }
  }

  private def auditTags = Map(
    "clientIP" -> AuditTags.trueClientIp,
    "path" -> AuditTags.requestPath,
    "X-Session-ID" -> AuditTags.rawSessionId,
    "Akamai-Reputation" -> AuditTags.akamaiReputationValue,
    "X-Request-ID" -> AuditTags.requestId,
    "deviceID" -> AuditTags.deviceId,
    "clientPort" -> AuditTags.trueClientPort
  )

}
