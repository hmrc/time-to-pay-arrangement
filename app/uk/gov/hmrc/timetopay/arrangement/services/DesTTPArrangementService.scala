package uk.gov.hmrc.timetopay.arrangement.services

import java.time.format.DateTimeFormatter

import play.api.Logger
import uk.gov.hmrc.timetopay.arrangement.models._
import uk.gov.hmrc.timetopay.arrangement.services.JurisdictionType.{JurisdictionType, Scottish}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DesTTPArrangementService extends DesTTPArrangementService {
}

trait DesTTPArrangementService {
  val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  def create(ttpArrangement: TTPArrangement): Future[DesTTPArrangement] = {
    Future {
      val schedule: Schedule = ttpArrangement.schedule
      val firstPaymentInstalment: Instalment = schedule.instalments.head

      val firstPayment = firstPaymentAmount(schedule)

      DesTTPArrangement(
        startDate = schedule.startDate,
        endDate = schedule.endDate,
        firstPaymentDate = firstPaymentInstalment.paymentDate,
        firstPaymentAmount = firstPayment.toString(),
        regularPaymentAmount = firstPaymentInstalment.amount.toString(),
        reviewDate = schedule.endDate.plusWeeks(3),
        enforcementAction = enforcementFlag(ttpArrangement.taxpayer).getOrElse(""),
        debitDetails = ttpArrangement.taxpayer.selfAssessment.debits,
        saNote = saNote(ttpArrangement)
      )
    }
  }

  private def firstPaymentAmount(schedule: Schedule): BigDecimal = {
    val firstPayment: Instalment = schedule.instalments.head
    val initialPayment = Option(schedule.initialPayment).getOrElse(BigDecimal(0.0))
    firstPayment.amount.+(initialPayment)
  }

  def enforcementFlag(taxpayer: Taxpayer) : Option[String]= {
    val addressTypes: Set[JurisdictionType] = taxpayer.addresses.map {
      JurisdictionChecker.addressType
    }.toSet

    addressTypes.size match {
      case 1 => addressTypes.head match {
        case Scottish => Some("Summary Warrant")
        case _ => Some("Distraint")
      }
      case _ =>
        Logger.info(s"Unable to determine enforcement flag as multiple jurisdiction detected $addressTypes")
        None
    }
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
      s"""SSTTP arrangement created online 1st payment amount of £$initialPayment
         |1st payment due date $paymentDueDate.
         |Regular payment amount £$regularPaymentAmount
         |Regular payment frequency monthly.
         |Review date $reviewDate DDI Ref. $directDebitReference PP Ref. $paymentPlanReference.
         |TTP letter issued
       """.stripMargin
    note
  }

}

