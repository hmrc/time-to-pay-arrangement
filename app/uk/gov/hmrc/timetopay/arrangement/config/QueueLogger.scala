/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.timetopay.arrangement.config

import play.api.Logger
import uk.gov.hmrc.timetopay.arrangement.model.{TTPArrangement, TTPArrangementWorkItem}
import uk.gov.hmrc.workitem.WorkItem

case class QueueLogger(log: Class[_]) {
  private val logger: Logger = Logger(log.getSimpleName)

  private val prefix = "ZONK-"
  private val prefixError = prefix + "ERROR"
  private val prefixMsg = prefix + "MSG"
  private val prefixTrace = prefix + "TRACE"

  def error(message: => String): Unit =
    logger.error(s"$prefixError $message")

  def error(message: => String, err: => Throwable): Unit =
    logger.error(s"$prefixError $message", err)

  def track(message: => String): Unit =
    logger.warn(s"$prefixMsg $message")

  def trace(utr: String, message: => String): Unit =
    logger.warn(s"$prefixTrace utr: $utr $message")

  def traceWorkItem(utr: String, wi: WorkItem[TTPArrangementWorkItem], message: => String): Unit = {
    val txt = s"receivedAt: ${wi.receivedAt}, updatedAt: ${wi.updatedAt}, availableAt: ${wi.availableAt}, status: ${wi.status}, failureCount: ${wi.failureCount}"
    val msg = s"TTPArrangementWorkItem for Reference ${wi.item.reference} ... {createdOn: ${wi.item.createdOn}, availableUntil: ${wi.item.availableUntil}"
    logger.warn(s"$prefixTrace utr: $utr $message workItem: $txt TTPArrangementWorkItem: $msg")
  }

  def trace(ttp: TTPArrangement, message: => String): Unit =
    trace(ttp.taxpayer.selfAssessment.utr, message)

  def traceError(ttp: TTPArrangement, message: => String): Unit =
    error(s"utr: ${ttp.taxpayer.selfAssessment.utr} $message")

  def traceError(ttp: TTPArrangement, message: => String, err: => Throwable): Unit =
    error(s"utr: ${ttp.taxpayer.selfAssessment.utr} $message", err)

}
