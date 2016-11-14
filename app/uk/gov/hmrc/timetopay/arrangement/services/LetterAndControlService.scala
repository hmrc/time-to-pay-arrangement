package uk.gov.hmrc.timetopay.arrangement.services

import play.api.{Logger}
import uk.gov.hmrc.timetopay.arrangement.config.LetterAndControlConfig
import uk.gov.hmrc.timetopay.arrangement.models._
import uk.gov.hmrc.timetopay.arrangement.services.JurisdictionType.JurisdictionType

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class LetterAndControlService(letterAndControlConfig : LetterAndControlConfig) {

  type AddressResult = (Address, Option[LetterError])

  case class LetterError (code: Int, message: String)

  object LetterError {
    def welshLargePrint() = LetterError(5, "welsh-large-print-required")
    def welshAudio() = LetterError(7,"audio-welsh-required")
    def welsh() = LetterError(4,"welsh-required")
    def braille() = LetterError(2,"braille-required")
    def audio() = LetterError(6,"audio-required")
    def largePrint() = LetterError(3, "large-print-required")
  }

  def create(ttpArrangement: TTPArrangement): Future[LetterAndControl] = Future {
    val taxpayer = ttpArrangement.taxpayer

    val correspondence: AddressResult = resolveAddress(ttpArrangement)

    val address: Address = correspondence._1
    val addressException:Option[LetterError] = correspondence._2

    val exception = addressException.map {
      exceptionCodeAndMessage
    }.getOrElse {
      taxpayer.selfAssessment.communicationPreferences.map {
       preference => resolveCommsPrefs(preference).map {
          exceptionCodeAndMessage
        }.getOrElse((None,None))
      }.getOrElse((None,None))
    }

    LetterAndControl(
      customerName = taxpayer.customerName,
      salutation = letterAndControlConfig.salutation,
      addressLine1 = address.addressLine1,
      addressLine2 = address.addressLine2,
      addressLine3 = address.addressLine3,
      addressLine4 = address.addressLine4,
      addressLine5 = address.addressLine5,
      postCode = address.postCode,
      totalAll = ttpArrangement.schedule.amountToPay.toString(),
      clmPymtString = paymentMessage(ttpArrangement.schedule),
      clmIndicateInt= letterAndControlConfig.claimIndicateInt,
      template = letterAndControlConfig.template,
      officeName1 = letterAndControlConfig.officeName1,
      officeName2 = letterAndControlConfig.officeName2,
      officePostcode = letterAndControlConfig.officePostCode,
      officePhone = letterAndControlConfig.officePhone,
      officeFax = letterAndControlConfig.officeFax,
      officeOpeningHours = letterAndControlConfig.officeOpeningHours,
      exceptionType = exception._1,
      exceptionReason = exception._2
    )

  }

  private def resolveAddress(ttpArrangement: TTPArrangement): AddressResult = {
    val taxpayer: Taxpayer = ttpArrangement.taxpayer
    taxpayer.addresses.size match {
      case 0 =>
        Logger.info("No address found in Digital")
        (Address(),Some(LetterError(8, "no-address")))
      case 1 =>
        validate(taxpayer.addresses.head)
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
        validate(taxpayer.addresses.head)
      case _ =>
        Logger.info(s"Customer has addresses in ${uniqueAddressTypes.mkString(" and")} jurisdictions")
        (Address(),Some(LetterError(1, "address-jurisdiction-conflict")))
    }
  }

  private def validate(address: Address): AddressResult = address match {
    case Address(_, _, _, _, _, "") | Address("", _, _, _, _, _) =>
      (address,Some(LetterError(9, "incomplete-address")))
    case _ =>
      (address, None)
  }

  private def resolveCommsPrefs(commsPrefs: CommunicationPreferences): Option[LetterError] = commsPrefs match {
      case CommunicationPreferences(true, _, true, _) =>
        Some(LetterError.welshLargePrint())
      case CommunicationPreferences(true, true, _, _) =>
        Some(LetterError.welshAudio())
      case CommunicationPreferences(true, _, _, _) =>
        Some(LetterError.welsh())
      case CommunicationPreferences(_, _, _, true) =>
        Some(LetterError.braille())
      case CommunicationPreferences(_, true, _, _) =>
        Some(LetterError.audio())
      case CommunicationPreferences(_, _, true, _) =>
        Some(LetterError.largePrint())
      case _ => None
    }

  private def exceptionCodeAndMessage(letter: LetterError) = (Some(letter.code.toString), Some(letter.message))

}
