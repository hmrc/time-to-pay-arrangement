package uk.gov.hmrc.timetopay.arrangement.services

import play.api.Logger
import uk.gov.hmrc.timetopay.arrangement.models._
import uk.gov.hmrc.timetopay.arrangement.services.JurisdictionType.JurisdictionType

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object LetterAndControlService extends LetterAndControlService {

}

trait LetterAndControlService {

  def create(implicit ttpArrangement: TTPArrangement): Future[LetterAndControl] = {
    Future {
      val taxpayer = ttpArrangement.taxpayer
      taxpayer.addresses.size match {
        case 0 =>
          Logger.info("Customer does not have an address listed in Digital")
          LetterAndControl(
            customerName = taxpayer.customerName,
            totalAll = ttpArrangement.schedule.amountToPay.toString(),
            clmPymtString = paymentMessage(ttpArrangement.schedule),
            exceptionType = Some("8"),
            exceptionReason = Some("no-address")
          )
        case 1 =>
         letterAndControl
        case _ => multipleAddresses(ttpArrangement)
      }
    }

  }

  private def multipleAddresses(implicit ttpArrangement: TTPArrangement) = {
    val taxpayer = ttpArrangement.taxpayer
    val addressTypes: Set[JurisdictionType] = taxpayer.addresses.map {
      JurisdictionChecker.addressType
    }.toSet

    addressTypes.size match {
      case 1 =>
        letterAndControl
      case _ =>
        Logger.info(s"Customer has addresses in ${addressTypes.mkString(" and")} jurisdictions")
        LetterAndControl(
          customerName = taxpayer.customerName,
          totalAll = ttpArrangement.schedule.amountToPay.toString(),
          clmPymtString = paymentMessage(ttpArrangement.schedule),
          exceptionType = Some("1"),
          exceptionReason = Some(s"address-jurisdiction-conflict")
        )
    }
  }

  private def paymentMessage(schedule: Schedule) = {
    val instalmentSize = schedule.instalments.size - 1
    val regularPaymentAmount = schedule.instalments.head.amount
    val lastPaymentAmount = schedule.instalments.last.amount

    val initialPayment = Option(schedule.initialPayment).getOrElse(BigDecimal(0.0)) + schedule.instalments.head.amount

    s"Initial payment of $initialPayment then $instalmentSize payments of $regularPaymentAmount and final payment of " +
      s"$lastPaymentAmount"
  }

  private def letterAndControl(implicit ttpArrangement: TTPArrangement): LetterAndControl = {
    val taxpayer = ttpArrangement.taxpayer
    val address = taxpayer.addresses.head

    address match {
      case Address(_, _, _, _, _, "") | Address("", _, _, _, _, _) =>
        LetterAndControl(
          customerName = taxpayer.customerName,
          totalAll= ttpArrangement.schedule.amountToPay.toString(),
          clmPymtString = paymentMessage(ttpArrangement.schedule),
          exceptionType = Some("9"),
          exceptionReason = Some("incomplete-address")
        )
      case _ =>
        LetterAndControl(
          customerName = taxpayer.customerName,
          addressLine1 = address.addressLine1,
          addressLine2 = address.addressLine2,
          addressLine3 = address.addressLine3,
          addressLine4 = address.addressLine4,
          addressLine5 = address.addressLine5,
          postCode = address.postCode,
          totalAll = ttpArrangement.schedule.amountToPay.toString(),
          clmPymtString = paymentMessage(ttpArrangement.schedule)
        )
    }
  }


}
