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

package uk.gov.hmrc.timetopay.arrangement.services

import java.time.format.DateTimeFormatter
import javax.inject.Inject
import play.api.{Configuration, Logger}
import uk.gov.hmrc.timetopay.arrangement._
import uk.gov.hmrc.timetopay.arrangement.config.JurisdictionCheckerConfig
import uk.gov.hmrc.timetopay.arrangement.model.{DesDebit, DesTTPArrangement, Instalment, PaymentSchedule, TTPArrangement, Taxpayer}
import uk.gov.hmrc.timetopay.arrangement.services.JurisdictionTypes.Scottish

import scala.math.BigDecimal.exact
import scala.math.Numeric.BigDecimalIsFractional

class DesTTPArrangementBuilder @Inject() (configuration: Configuration) {
  val logger: Logger = Logger(getClass)

  val jurisdictionChecker: JurisdictionChecker = new JurisdictionChecker(JurisdictionCheckerConfig.create(configuration))

  val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  def create(implicit ttpArrangement: TTPArrangement): DesTTPArrangement = {

    val schedule: PaymentSchedule = ttpArrangement.schedule
    val firstPaymentInstalment: Instalment = schedule.instalments.head

    DesTTPArrangement(
      startDate            = schedule.startDate,
      endDate              = schedule.endDate,
      firstPaymentDate     = firstPaymentInstalment.paymentDate,
      firstPaymentAmount   = schedule.initialPayment.setScale(2).toString(),
      regularPaymentAmount = firstPaymentInstalment.amount.setScale(2).toString(),
      reviewDate           = schedule.instalments.last.paymentDate.plusWeeks(3),
      enforcementAction    = enforcementFlag(ttpArrangement.taxpayer),
      debitDetails         = ttpArrangement.taxpayer.selfAssessment.debits.map { d => DesDebit(d.originCode, d.dueDate) },
      saNote               = saNote(ttpArrangement))
  }

  /**
   * Uses the taxpayers post code to set the enforcementFlag
   * 1. If the tax payer's address is in England, enter "Distraint"
   * 2. If the tax payer's address in in Scotland, enter "Summary Warrant"
   * 3. If the tax payer has addresses in both regions, enter "Other"
   * 4. If the tax payer's address is a bad address (so we can't determine the region), enter "Other"
   */
  def enforcementFlag(taxpayer: Taxpayer): String = {
    val addressTypes: List[JurisdictionType] = taxpayer.addresses.flatMap {
      jurisdictionChecker.addressType
    }.distinct

    addressTypes match {
      case x :: Nil => x match {
        case Scottish => "Summary Warrant"
        case _        => "Distraint"
      }
      case _ =>
        logger.info(s"Unable to determine enforcement flag as multiple mixed or no jurisdictions detected $addressTypes")
        "Other"
    }
  }

  def saNote(ttpArrangement: TTPArrangement): String = {
    val schedule: PaymentSchedule = ttpArrangement.schedule
    val initialPayment = Option(schedule.initialPayment).getOrElse(BigDecimal(0.0)).setScale(2)
    val reviewDate = schedule.endDate.plusWeeks(3).format(formatter)
    val regularPaymentAmount = schedule.instalments.head.amount.setScale(2)
    val initialPaymentDate = schedule.instalments.head.paymentDate.format(formatter)
    val startDate = schedule.startDate.format(formatter)
    val endDate = schedule.endDate.format(formatter)
    val directDebitReference = ttpArrangement.directDebitReference
    val paymentPlanReference = ttpArrangement.paymentPlanReference
    val finalPayment = schedule.instalments.last.amount.setScale(2)

    val saNotes =
      s"DDI $directDebitReference, PP $paymentPlanReference, " +
        s"${if (initialPayment > exact(0)) s"initial payment of £$initialPayment on $startDate, " else ""}" +
        s"first regular payment of £$regularPaymentAmount " +
        s"from $initialPaymentDate, frequency monthly, final payment of £$finalPayment on $endDate, " +
        s"review date $reviewDate"

    saNotes.take(250)
  }
}
