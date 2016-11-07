package uk.gov.hmrc.timetopay.arrangement.services

import uk.gov.hmrc.timetopay.arrangement.models.{LetterAndControl, Schedule, TTPArrangement}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object LetterAndControlService extends LetterAndControlService {

}

/**
  * Further changes happening to this class
  */
trait LetterAndControlService {

  def create(ttpArrangement: TTPArrangement): Future[LetterAndControl] = {
    Future {
      val taxpayer = ttpArrangement.taxpayer

      taxpayer.addresses.size match {
        case 0 =>
          LetterAndControl(
            customerName = taxpayer.customerName,
            salutation = "Dear Sir or Madam",
            totalAll = ttpArrangement.schedule.amountToPay.toString(),
            clmIndicateInt = "Interest is due",
            clmPymtString = paymentMessage(ttpArrangement.schedule),
            template = "template",
            exceptionType = Some("1"),
            exceptionReason = Some("No address found")
          )
        case 1 =>
          taxpayer.addresses.map {
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
          }.head
        case _ => determineLetterAndControl(ttpArrangement)
      }
    }

  }

  private def determineLetterAndControl(arrangement: TTPArrangement) = {
    val taxpayer = arrangement.taxpayer
    LetterAndControl(
      customerName = taxpayer.customerName,
      salutation = "Dear Sir or Madam",
      totalAll = arrangement.schedule.amountToPay.toString(),
      clmIndicateInt = "Interest is due",
      clmPymtString = paymentMessage(arrangement.schedule),
      template = "template",
      exceptionType = Some("3"),
      exceptionReason = Some("More than one address found")
    )
  }

  private def paymentMessage(schedule: Schedule) = {
    val instalmentSize = schedule.instalments.size - 1
    val regularPaymentAmount = schedule.instalments.head.amount
    val lastPaymentAmount = schedule.instalments.last.amount

    val initialPayment = Option(schedule.initialPayment).getOrElse(BigDecimal(0.0)) + schedule.instalments.head.amount

    s"Initial payment of $initialPayment then $instalmentSize payments of $regularPaymentAmount and final payment of " +
      s"$lastPaymentAmount"
  }

}
