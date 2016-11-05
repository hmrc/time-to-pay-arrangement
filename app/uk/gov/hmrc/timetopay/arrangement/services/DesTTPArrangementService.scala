package uk.gov.hmrc.timetopay.arrangement.services

import uk.gov.hmrc.timetopay.arrangement.models.{DesTTPArrangement, Instalment, Schedule, TTPArrangement}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
object DesTTPArrangementService extends DesTTPArrangementService {

}

trait DesTTPArrangementService {

  def create(ttpArrangement: TTPArrangement): Future[DesTTPArrangement] = {
    Future {

      val schedule: Schedule = ttpArrangement.schedule
      val firstPayment: Instalment = schedule.instalments.head

      val initialPayment = Option(schedule.initialPayment).getOrElse(BigDecimal(0.0))
      val firstPaymentAmount = firstPayment.amount.+(initialPayment)

      DesTTPArrangement(
        startDate =schedule.startDate,
        endDate = schedule.endDate,
        firstPaymentDate = firstPayment.paymentDate,
        firstPaymentAmount = firstPaymentAmount.toString(),
        regularPaymentAmount = firstPayment.amount.toString(),
        regularPaymentFrequency = "Monthly",
        reviewDate = schedule.endDate.plusWeeks(3),
        enforcementAction = enforcementFlag(ttpArrangement),
        directDebit = true,
        debitDetails = ttpArrangement.taxpayer.selfAssessment.debits,
        saNote = saNote(ttpArrangement)
      )
    }
  }

  private def enforcementFlag(tTPArrangement: TTPArrangement) =  {
    //TODO: implement different flags
    "Distraint"
  }

  private def saNote(ttpArrangement: TTPArrangement)  = {
    val saNote = s"Direct Debit Reference ${ttpArrangement.directDebitReference} Payment Plan Reference ${ttpArrangement.paymentPlanReference}"
    saNote
  }

}

