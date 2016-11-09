package uk.gov.hmrc.timetopay.arrangement.services

import play.api.Logger
import uk.gov.hmrc.timetopay.arrangement.models._
import uk.gov.hmrc.timetopay.arrangement.services.JurisdictionType.JurisdictionType

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object LetterAndControlService extends LetterAndControlService {

}


trait LetterAndControlService {

  type AddressResult = (Address, Option[LetterError])

  case class LetterError (code: Int, message: String)


  def create(implicit ttpArrangement: TTPArrangement): Future[LetterAndControl] = Future {
    val taxpayer = ttpArrangement.taxpayer

    val correspondence : AddressResult = resolveCorrespondence
    val address : Address = correspondence._1
    val exception = correspondence._2 match {
      case Some(l) => Some(l.code.toString) -> Some(l.message)
      case _ => None -> None
    }

    LetterAndControl(

      customerName = taxpayer.customerName,
      addressLine1 = address.addressLine1,
      addressLine2 = address.addressLine2,
      addressLine3 = address.addressLine3,
      addressLine4 = address.addressLine4,
      addressLine5 = address.addressLine5,
      postCode = address.postCode,
      totalAll = ttpArrangement.schedule.amountToPay.toString(),
      clmPymtString = paymentMessage(ttpArrangement.schedule),
      exceptionType = exception._1,
      exceptionReason = exception._2
    )

  }


  private def resolveCorrespondence(implicit ttpArrangement: TTPArrangement) : AddressResult = {
    val taxpayer: Taxpayer = ttpArrangement.taxpayer
    taxpayer.addresses.size match {
      case 0 =>
        Logger.info("No address found in Digital")
        Address() -> Some(LetterError(8, "no-address"))
      case 1 =>
        singleAddress(taxpayer.addresses.head)
      case _ =>
        multipleAddresses(taxpayer)
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

  private def multipleAddresses(implicit taxpayer: Taxpayer) = {
    val uniqueAddressTypes: Set[JurisdictionType] = taxpayer.addresses.map {
      JurisdictionChecker.addressType
    }.toSet

    uniqueAddressTypes.size match {
      case 1 =>
        singleAddress(taxpayer.addresses.head)
      case _ =>
        Logger.info(s"Customer has addresses in ${uniqueAddressTypes.mkString(" and")} jurisdictions")
        Address() -> Some(LetterError(1, "address-jurisdiction-conflict"))
    }
  }

  private def singleAddress(address: Address) : AddressResult ={
    address match {
      case Address(_, _, _, _, _, "") | Address("", _, _, _, _, _) =>
        address -> Some(LetterError(9, "incomplete-address"))
      case _ =>
        address -> None
    }
  }

}
