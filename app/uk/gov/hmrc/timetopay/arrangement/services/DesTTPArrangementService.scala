package uk.gov.hmrc.timetopay.arrangement.services

import uk.gov.hmrc.timetopay.arrangement.models.{DesTTPArrangement, Instalment, Schedule, TTPArrangement}

object DesTTPArrangementService extends DesTTPArrangementService {

}

trait DesTTPArrangementService {

  def create(ttpArrangement: TTPArrangement): DesTTPArrangement = {

    val saNote = s"Direct Debit Reference ${ttpArrangement.directDebitReference} Payment Plan Reference ${ttpArrangement.paymentPlanReference}"

    val schedule: Schedule = ttpArrangement.schedule
    val firstPayment: Instalment = schedule.instalments.head

    DesTTPArrangement(
      schedule.startDate,
      schedule.endDate,
      firstPayment.paymentDate,
      firstPayment.amount.toString(),
      firstPayment.amount.toString(),
      "Monthly",
      schedule.endDate.plusWeeks(3),
      "DOM",
      "Distraint",
      directDebit = true,
      ttpArrangement.taxpayer.selfAssessment.debits,
      saNote
    )
  }

  private def enforcementFlag(tTPArrangement: TTPArrangement) =  {
    //TODO: implement different flags
    "Distraint"
  }


}

