package uk.gov.hmrc.timetopay.arrangement.services

import play.api.Play.application
import uk.gov.hmrc.timetopay.arrangement.Address


object JurisdictionType extends Enumeration  {
  type JurisdictionType = Value
  val English, Scottish, Welsh = Value
}

object JurisdictionChecker {
  import uk.gov.hmrc.timetopay.arrangement.services.JurisdictionType._
  import play.api.Play.current

  val scottishPostCodeRegex = application.configuration.getString("scottish.postcode.prefix")
    .getOrElse(throw new RuntimeException("Scottish postcode prefix needed")).r
  val welshPostCodeRegex = application.configuration.getString("welsh.postcode.prefix")
    .getOrElse(throw new RuntimeException("Welsh postcode prefix needed")).r

  def addressType(address: Address): JurisdictionType = {
     address.postcode match {
       case scottishPostCodeRegex(_) => Scottish
       case welshPostCodeRegex(_) => Welsh
       case _ =>  English
     }
  }
}
