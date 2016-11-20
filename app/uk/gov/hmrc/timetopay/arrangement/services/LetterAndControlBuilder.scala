package uk.gov.hmrc.timetopay.arrangement.services

import play.api.Logger
import uk.gov.hmrc.timetopay.arrangement._
import uk.gov.hmrc.timetopay.arrangement.config.LetterAndControlConfig
import uk.gov.hmrc.timetopay.arrangement.services.JurisdictionType.JurisdictionType

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class LetterAndControlBuilder(letterAndControlConfig: LetterAndControlConfig) {

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

    def resolveCommsException = {
      (for {
        c <- taxpayer.selfAssessment.communicationPreferences
        e <- commsPrefException(c)
      } yield (Some(e.code.toString), Some(e.message))).getOrElse((None,None))
    }

    val exception = correspondence._2.fold(resolveCommsException)(x => (Some(x.code.toString), Some(x.message)))

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
    implicit val taxpayer: Taxpayer = ttpArrangement.taxpayer
    taxpayer.addresses match {
      case Nil =>
        Logger.info("No address found in Digital")
        (Address(),Some(LetterError(8, "no-address")))
      case x::Nil =>
        validate(x)
      case _ =>
        multipleAddresses
    }
  }


  private def multipleAddresses(implicit taxpayer: Taxpayer) = {
    val uniqueAddressTypes: List[JurisdictionType] = taxpayer.addresses.map {
      JurisdictionChecker.addressType
    }.distinct

    uniqueAddressTypes match {
      case x::Nil =>
        Logger.trace("Found single unique address type found")
        validate(taxpayer.addresses.head)
      case _ =>
        Logger.trace(s"Customer has addresses in ${uniqueAddressTypes.mkString(" and")} jurisdictions")
        (Address(),Some(LetterError(1, "address-jurisdiction-conflict")))
    }
  }

  def validate(address: Address) = address match {
    case Address(_, _, _, _, _, "") | Address("", _, _, _, _, _) =>
      (address,Some(LetterError(9, "incomplete-address")))
    case _ =>
      (address, None)
  }


  private def paymentMessage(schedule: Schedule) = {
    val instalmentSize = schedule.instalments.size - 1
    val regularPaymentAmount = schedule.instalments.head.amount
    val lastPaymentAmount = schedule.instalments.last.amount

    val initialPayment = Try(schedule.initialPayment).getOrElse(BigDecimal(0.0)) + schedule.instalments.head.amount

    s"Initial payment of $initialPayment then $instalmentSize payments of $regularPaymentAmount and final payment of " +
      s"$lastPaymentAmount"
  }

  private def commsPrefException(commsPrefs: CommunicationPreferences): Option[LetterError] = commsPrefs match {
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

}
