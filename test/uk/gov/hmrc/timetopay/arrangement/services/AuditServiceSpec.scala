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

package uk.gov.hmrc.timetopay.arrangement.services

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.{AuditChannel, AuditConnector, AuditResult, DatastreamMetrics}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import uk.gov.hmrc.timetopay.arrangement.connectors.SubmissionError
import uk.gov.hmrc.timetopay.arrangement.model.TTPArrangementWorkItem
import uk.gov.hmrc.timetopay.arrangement.support.{ITSpec, TestData}

import java.time.LocalDateTime
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

  "audit event for sendArrangementQueuedEvent" in {
    val submissionError = SubmissionError(999, "error-message")
    val timeNow = LocalDateTime.now()
    val workItem = TTPArrangementWorkItem(
      timeNow,
      timeNow.plusDays(2),
      "referenceAsString",
      "ttpArrangementAsString",
      "someAuditTags"
    )

    val expected: JsValue =
      Json.parse(
        s"""{
           |  "status":"direct debit instruction success | TTP arrangement failed temporarily (DES server error) | Queued for retry",
           |  "submissionError":{
           |    "code":999,
           |    "message":"error-message"
           |  },
           |  "directDebitInstructionReference": "12345678901234567890",
           |  "paymentPlanReference": "12345678901234567890",
           |  "workItem":{
           |    "createdOn":"$timeNow",
           |    "availableUntil":"${timeNow.plusDays(2)}"
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
    auditService.sendArrangementQueuedEvent(taxpayer, bankDetails, paymentSchedule, submissionError, ttpArrangement, workItem, auditTags)
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
