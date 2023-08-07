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

import play.api.libs.json.{JsObject, Json}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.timetopay.arrangement.connectors.SubmissionError
import uk.gov.hmrc.timetopay.arrangement.model.{BankDetails, PaymentSchedule, TTPArrangement, TTPArrangementWorkItem, Taxpayer}

import java.time.temporal.ChronoUnit
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AuditService @Inject() (auditConnector: AuditConnector)(implicit ec: ExecutionContext) {

  def sendSubmissionSucceededEvent(
      arrangement: TTPArrangement,
      auditTags:   Map[String, String]
  ): Unit = {

    val event = makeEvent(
      arrangement.taxpayer,
      arrangement.bankDetails,
      arrangement.schedule,
      auditTags,
      Json.obj(
        "status" -> "successfully submitted direct debit and TTP Arrangement",
        "directDebitInstructionReference" -> arrangement.directDebitReference,
        "paymentPlanReference" -> arrangement.paymentPlanReference
      ))
    auditConnector.sendExtendedEvent(event)
    ()
  }

  def sendArrangementQueuedEvent(
      arrangement:     TTPArrangement,
      submissionError: SubmissionError,
      workItem:        TTPArrangementWorkItem,
      auditTags:       Map[String, String]
  ): Unit = {
    val event = makeEvent(
      arrangement.taxpayer,
      arrangement.bankDetails,
      arrangement.schedule,
      auditTags,
      Json.obj(
        "status" -> "direct debit instruction success but TTP arrangement failed temporarily (DES server error) - Queued for retry",
        "submissionError" -> submissionError,
        "directDebitInstructionReference" -> arrangement.directDebitReference,
        "paymentPlanReference" -> arrangement.paymentPlanReference,
        "workItem" -> Json.obj(
          "createdOn" -> workItem.createdOn.toString,
          "availableUntil" -> workItem.availableUntil.toString
        )
      )
    )
    auditConnector.sendExtendedEvent(event)
    ()
  }

  def sendArrangementSubmissionFailedEvent(
      arrangement:     TTPArrangement,
      submissionError: SubmissionError,
      auditTags:       Map[String, String]
  ): Unit = {
    val event = makeEvent(
      arrangement.taxpayer,
      arrangement.bankDetails,
      arrangement.schedule,
      auditTags,
      Json.obj(
        "status" -> "submitted direct debit but failed to submit TTP Arrangement",
        "submissionError" -> submissionError,
        "directDebitInstructionReference" -> arrangement.directDebitReference,
        "paymentPlanReference" -> arrangement.paymentPlanReference,
      ))
    auditConnector.sendExtendedEvent(event)
    ()
  }

  private def makeEvent(
      taxpayer:    Taxpayer,
      bankDetails: BankDetails,
      schedule:    PaymentSchedule,
      auditTags:   Map[String, String],
      extraInfo:   JsObject
  ) = ExtendedDataEvent(
    auditSource = "pay-what-you-owe",
    //`directDebitSetup` was provided at the beginning.
    // I'm not changing it to make splunk events consistent with what we already have stored
    auditType = "directDebitSetup",
    tags      = auditTags,
    detail    = extraInfo ++ makeDetails(taxpayer, bankDetails, schedule)
  )

  private def makeDetails(taxpayer: Taxpayer, bankDetails: BankDetails, schedule: PaymentSchedule) = Json.obj(
    "utr" -> taxpayer.selfAssessment.utr,
    "bankDetails" -> Json.obj(
      "name" -> bankDetails.accountName,
      "accountNumber" -> bankDetails.accountNumber,
      "sortCode" -> bankDetails.sortCode
    ),
    "schedule" -> Json.obj(
      "initialPaymentAmount" -> schedule.initialPayment,
      "installments" -> Json.toJson(schedule.instalments.sortBy(_.paymentDate.toEpochDay)),
      "numberOfInstallments" -> schedule.instalments.length,
      "installmentLengthCalendarMonths" -> ChronoUnit.MONTHS.between(schedule.startDate, schedule.endDate),
      "totalPaymentWithoutInterest" -> schedule.amountToPay,
      "totalInterestCharged" -> schedule.totalInterestCharged,
      "totalPayable" -> schedule.totalPayable)
  )

}

object AuditService {
  def auditTags(implicit request: RequestHeader): Map[String, String] = {
    val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    Map(
      "Akamai-Reputation" -> hc.akamaiReputation.map(_.value).getOrElse("-"),
      "X-Request-ID" -> hc.requestId.map(_.value).getOrElse("-"),
      "X-Session-ID" -> hc.sessionId.map(_.value).getOrElse("-"),
      "clientIP" -> hc.trueClientIp.getOrElse("-"),
      "clientPort" -> hc.trueClientPort.getOrElse("-"),
      "path" -> request.path,
      "deviceID" -> hc.deviceID.getOrElse("-")
    )
  }
}
