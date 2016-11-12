package uk.gov.hmrc.timetopay.arrangement.config

import play.api.Configuration


case class LetterAndControlConfig (salutation: String,
                                   claimIndicateInt: String,
                                   template: String,
                                   officeName1: String,
                                   officeName2: String,
                                   officePostCode: String,
                                   officePhone: String,
                                   officeFax: String,
                                   officeOpeningHours: String ){
}

object LetterAndControlConfig {


  def create(configuration: Configuration) = {

    def getConfig(key: String) = {
      configuration.getString(key).getOrElse(throw new IllegalArgumentException(s"Missing $key"))
    }

    LetterAndControlConfig( getConfig("salutation"),
      getConfig("claimIndicateInt") ,
      getConfig("template"),
      getConfig("office.officeName1"),
      getConfig("office.officeName2"),
      getConfig("office.officePostCode"),
      getConfig("office.officePhone"),
      getConfig("office.officeFax"),
      getConfig("office.officeOpeningHours")
    )

  }
}
