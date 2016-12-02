package uk.gov.hmrc.timetopay.arrangement.services

import java.time.format.DateTimeFormatter

import play.api.Logger
import uk.gov.hmrc.timetopay.arrangement._
import uk.gov.hmrc.timetopay.arrangement.services.JurisdictionType.{JurisdictionType, Scottish}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class DesTTPArrangementBuilder {
  val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  def create(implicit ttpArrangement: TTPArrangement): DesTTPArrangement =  {
    val schedule: Schedule = ttpArrangement.schedule
    val firstPaymentInstalment: Instalment = schedule.instalments.head

    val firstPayment = firstPaymentAmount(schedule)

    DesTTPArrangement(
      startDate = schedule.startDate,
      endDate = schedule.endDate,
      firstPaymentDate = firstPaymentInstalment.paymentDate,
      firstPaymentAmount = firstPayment.toString(),
      regularPaymentAmount = firstPaymentInstalment.amount.toString(),
      reviewDate = schedule.instalments.last.paymentDate.plusWeeks(3),
      enforcementAction = enforcementFlag(ttpArrangement.taxpayer).getOrElse("Other"),
      debitDetails = ttpArrangement.taxpayer.selfAssessment.debits.map { d => DesDebit(d.originCode, d.dueDate) },
      saNote = saNote(ttpArrangement)
    )
  }

  def enforcementFlag(taxpayer: Taxpayer): Option[String]= {
    val addressTypes: List[JurisdictionType] = taxpayer.addresses.map {
      JurisdictionChecker.addressType
    }.distinct

    addressTypes match {
      case x::Nil => x match {
        case Scottish => Some("Summary Warrant")
        case _ => Some("Distraint")
      }
      case _ =>
        Logger.info(s"Unable to determine enforcement flag as multiple mixed or no jurisdictions detected $addressTypes")
        Some("Other")
    }
  }

  private def firstPaymentAmount(schedule: Schedule): BigDecimal = {
    val firstPayment: Instalment = schedule.instalments.head
    val initialPayment = Option(schedule.initialPayment).getOrElse(BigDecimal(0.0))
    firstPayment.amount.+(initialPayment)
  }

  private def saNote(ttpArrangement: TTPArrangement) = {
    val schedule: Schedule = ttpArrangement.schedule
    val initialPayment = firstPaymentAmount(schedule)
    val reviewDate = schedule.endDate.plusWeeks(3).format(formatter)
    val regularPaymentAmount = schedule.instalments.head.amount
    val paymentDueDate = ttpArrangement.schedule.startDate.format(formatter)
    val directDebitReference = ttpArrangement.directDebitReference
    val paymentPlanReference = ttpArrangement.paymentPlanReference
    val note =
      s"SSTTP arrangement created online 1st payment amount of £$initialPayment 1st payment due date $paymentDueDate." +
        s"Regular payment amount £$regularPaymentAmount. Regular payment frequency monthly. " +
        s"Review date $reviewDate DDI Ref. $directDebitReference PP Ref. $paymentPlanReference." +
        s"TTP letter issued"
    note
  }
}

