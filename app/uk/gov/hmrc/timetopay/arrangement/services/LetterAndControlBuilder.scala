/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.timetopay.arrangement.services

import javax.inject.Inject
import play.api.{Configuration, Logger}
import uk.gov.hmrc.timetopay.arrangement._
import uk.gov.hmrc.timetopay.arrangement.config.{JurisdictionCheckerConfig, LetterAndControlAndJurisdictionChecker}

import scala.util.Try

class LetterAndControlBuilder @Inject() (letterAndControlAndJurisdictionChecker: LetterAndControlAndJurisdictionChecker, configuration: Configuration) {
  type AddressResult = (Address, Option[LetterError])

  private val jurisdictionChecker = new JurisdictionChecker(JurisdictionCheckerConfig.create(configuration))
  private val LetterAndControlConfig = letterAndControlAndJurisdictionChecker.createLetterAndControlConfig

  def create(ttpArrangement: TTPArrangement): LetterAndControl = {
    val taxpayer = ttpArrangement.taxpayer

    val correspondence: AddressResult = resolveAddress(ttpArrangement)

    val address: Address = validateAddressFormat(correspondence._1)

      def resolveCommsException = {
        (for {
          c <- taxpayer.selfAssessment.communicationPreferences
          e <- commsPrefException(c)
        } yield (Some(e.code.toString), Some(e.message))).getOrElse((None, None))
      }

    val exception = correspondence._2.fold(resolveCommsException)(x => (Some(x.code.toString), Some(x.message)))

    val customerName = taxpayer.customerName

    LetterAndControl(
      customerName       = customerName,
      salutation         = s"${LetterAndControlConfig.salutation}$customerName",
      addressLine1       = address.addressLine1,
      addressLine2       = address.addressLine2,
      addressLine3       = address.addressLine3,
      addressLine4       = address.addressLine4,
      addressLine5       = address.addressLine5,
      postCode           = address.postcode,
      totalAll           = ttpArrangement.schedule.totalPayable.setScale(2).toString(),
      clmPymtString      = paymentMessage(ttpArrangement.schedule),
      clmIndicateInt     = LetterAndControlConfig.claimIndicateInt,
      template           = LetterAndControlConfig.template,
      officeName1        = LetterAndControlConfig.officeName1,
      officeName2        = LetterAndControlConfig.officeName2,
      officePostcode     = LetterAndControlConfig.officePostCode,
      officePhone        = LetterAndControlConfig.officePhone,
      officeFax          = LetterAndControlConfig.officeFax,
      officeOpeningHours = LetterAndControlConfig.officeOpeningHours,
      exceptionType      = exception._1,
      exceptionReason    = exception._2)

  }

  private def paymentMessage(schedule: Schedule) = {
    val instalmentSize = schedule.instalments.size - 2
    val regularPaymentAmount = schedule.instalments.head.amount.setScale(2)
    val lastPaymentAmount = schedule.instalments.last.amount.setScale(2)

    val initialPayment = (Try(schedule.initialPayment).getOrElse(BigDecimal(0.0)) + schedule.instalments.head.amount).setScale(2)

    instalmentSize match {
      case 0 => f"Initial payment of £$initialPayment%,.2f then a final payment of £$lastPaymentAmount%,.2f"
      case _ => f"Initial payment of £$initialPayment%,.2f then $instalmentSize payments of £$regularPaymentAmount%,.2f and final payment of £" +
        f"$lastPaymentAmount%,.2f"
    }
  }

  private def validateAddressFormat(address: Address): Address = Address(
    addressLine1 = address.addressLine1,
    addressLine2 = if (address.addressLine2.getOrElse("").equals("")) None else address.addressLine2,
    addressLine3 = if (address.addressLine3.getOrElse("").equals("")) None else address.addressLine3,
    addressLine4 = if (address.addressLine4.getOrElse("").equals("")) None else address.addressLine4,
    addressLine5 = if (address.addressLine5.getOrElse("").equals("")) None else address.addressLine5,
    postcode     = address.postcode)

  private def resolveAddress(ttpArrangement: TTPArrangement): AddressResult = {
    implicit val taxpayer: Taxpayer = ttpArrangement.taxpayer
    taxpayer.addresses match {
      case Nil =>
        Logger.logger.debug("No address found in Digital")
        (Address(), Some(LetterError(8, "no address")))
      case x :: Nil =>
        Logger.logger.debug("Found single address")
        validate(x)
      case _ =>
        Logger.logger.debug("Found multiple addresses")
        multipleAddresses
    }
  }

  private def multipleAddresses(implicit taxpayer: Taxpayer) = {
    val uniqueAddressTypes: List[JurisdictionType] = taxpayer.addresses.flatMap {
      jurisdictionChecker.addressType
    }.distinct

    uniqueAddressTypes match {
      case _ :: Nil =>
        Logger.logger.trace("Found single unique address type found")
        validate(taxpayer.addresses.head)
      case _ =>
        Logger.logger.trace(s"Customer has addresses in ${uniqueAddressTypes.mkString(" and")} jurisdictions")
        (Address(), Some(LetterError(1, "address jurisdiction conflict")))
    }
  }

  private def validate(address: Address) = address match {
    case Address(_, _, _, _, _, None | Some("")) | Address("", _, _, _, _, _) =>
      (address, Some(LetterError(9, "incomplete address")))
    case _ =>
      (address, None)
  }

  private def commsPrefException(commsPrefs: CommunicationPreferences): Option[LetterError] = commsPrefs match {
    case CommunicationPreferences(true, _, true, _) =>
      Logger.logger.debug(s"Exception found in LetterAndControl - Code: 5 Reason: Welsh large print required")
      Some(LetterError.welshLargePrint())
    case CommunicationPreferences(true, true, _, _) =>
      Logger.logger.debug(s"Exception found in LetterAndControl - Code: 7 Reason: Audio Welsh required")
      Some(LetterError.welshAudio())
    case CommunicationPreferences(true, _, _, _) =>
      Logger.logger.debug(s"Exception found in LetterAndControl - Code: 4 Reason: Welsh required")
      Some(LetterError.welsh())
    case CommunicationPreferences(_, _, _, true) =>
      Logger.logger.debug(s"Exception found in LetterAndControl - Code: 2 Reason: Braille required")
      Some(LetterError.braille())
    case CommunicationPreferences(_, true, _, _) =>
      Logger.logger.debug(s"Exception found in LetterAndControl - Code: 6 Reason: Audio required")
      Some(LetterError.audio())
    case CommunicationPreferences(_, _, true, _) =>
      Logger.logger.debug(s"Exception found in LetterAndControl - Code: 3 Reason: Large print required")
      Some(LetterError.largePrint())
    case _ => None
  }

  case class LetterError(code: Int, message: String)

  object LetterError {
    def welshLargePrint() = LetterError(5, "welsh large print required")

    def welshAudio() = LetterError(7, "audio welsh required")

    def welsh() = LetterError(4, "welsh required")

    def braille() = LetterError(2, "braille required")

    def audio() = LetterError(6, "audio required")

    def largePrint() = LetterError(3, "large print required")
  }

}
