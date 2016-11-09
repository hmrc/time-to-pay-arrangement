package uk.gov.hmrc.timetopay.arrangement.services

import play.api.Logger
import uk.gov.hmrc.timetopay.arrangement.models._
import uk.gov.hmrc.timetopay.arrangement.services.JurisdictionType.JurisdictionType

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class LetterAndControlService {

  type AddressResult = (Address, Option[LetterError])

  case class LetterError (code: Int, message: String)

  def create(implicit ttpArrangement: TTPArrangement): Future[LetterAndControl] = Future {
    val taxpayer = ttpArrangement.taxpayer

    val correspondence: AddressResult = resolveAddress

    val address: Address = correspondence._1
    val addressException:Option[LetterError] = correspondence._2

    val exception = addressException.map {
      exceptionCodeAndMessage
    }.getOrElse {
      val preferences = taxpayer.selfAssessment.communicationPreferences
      resolveCommsPrefs(preferences).map {
        exceptionCodeAndMessage
      }.getOrElse(None -> None)
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

  private def resolveAddress(implicit ttpArrangement: TTPArrangement): AddressResult = {
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

  private def singleAddress(address: Address): AddressResult = address match {
    case Address(_, _, _, _, _, "") | Address("", _, _, _, _, _) =>
      address -> Some(LetterError(9, "incomplete-address"))
    case _ =>
      address -> None
  }


  private def resolveCommsPrefs(commsPrefs: CommunicationPreferences): Option[LetterError] = commsPrefs match {
      case CommunicationPreferences(true, _, true, _) =>
        Some(LetterError(5, "welsh-large-print-required"))
      case CommunicationPreferences(true, true, _, _) =>
        Some(LetterError(7,"audio-welsh-required"))
      case CommunicationPreferences(true, _, _, _) =>
        Some(LetterError(4,"welsh-required"))
      case CommunicationPreferences(_, _, _, true) =>
        Some(LetterError(2,"braille-required"))
      case CommunicationPreferences(_, true, _, _) =>
        Some(LetterError(6,"audio-required"))
      case CommunicationPreferences(_, _, true, _) =>
        Some(LetterError(3, "large-print-required"))
      case _ => None
    }

  private def exceptionCodeAndMessage(letter: LetterError) = {
    Some(letter.code.toString) -> Some(letter.message)
  }

}
