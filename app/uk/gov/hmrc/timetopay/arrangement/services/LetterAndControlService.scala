package uk.gov.hmrc.timetopay.arrangement.services

import uk.gov.hmrc.timetopay.arrangement.models.{LetterAndControl, Schedule, TTPArrangement}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object LetterAndControlService extends LetterAndControlService {

}

trait LetterAndControlService {

  def create(ttpArrangement: TTPArrangement): Future[LetterAndControl] = {
    Future {
      val taxpayer = ttpArrangement.taxpayer
      val letterAndControls: List[LetterAndControl] = taxpayer.selfAssessment.addresses.map {
        address => {
          LetterAndControl(
            taxpayer.customerName,
            "Dear Sir or Madam",
            address.addressLine1,
            address.addressLine2,
            address.addressLine3,
            address.addressLine4,
            address.addressLine5,
            address.postCode,
            ttpArrangement.schedule.amountToPay.toString(),
            "Interest is due",
            paymentMessage(ttpArrangement.schedule),
            "",
            "",
            "",
            "",
            "",
            "",
            "template",
            None,
            None
          )
        }
      }
      letterAndControls.head
    }
  }

  private def paymentMessage(schedule: Schedule) = {
    val instalmentSize = schedule.instalments.size - 1

    s"Initial payment of ${schedule.initialPayment} then $instalmentSize payments of ${schedule.instalments.head.amount} and final payment of " +
      s"${schedule.instalments.last.amount}"
  }

}
