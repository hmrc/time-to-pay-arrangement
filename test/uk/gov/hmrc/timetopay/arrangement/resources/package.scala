package uk.gov.hmrc.timetopay.arrangement

import play.api.libs.json.Json
import uk.gov.hmrc.timetopay.arrangement.models.{Taxpayer, LetterAndControl, DesTTPArrangement}

import scala.io.Source
import uk.gov.hmrc.timetopay.arrangement.modelsFormat._

package object resources {

  val ttparrangementRequest = Json.parse(Source.fromFile(s"test/uk/gov/hmrc/timetopay/arrangement/resources/TTPArrangementRequest.json").getLines.mkString)
  val ttparrangementResponse = Json.parse(Source.fromFile(s"test/uk/gov/hmrc/timetopay/arrangement/resources/TTPArrangementResponse.json").getLines.mkString)
  val submitArrangementTTPArrangement: DesTTPArrangement =
    Json.parse(Source.fromFile(s"test/uk/gov/hmrc/timetopay/arrangement/resources/DesTTPArrangement.json").getLines.mkString).as[DesTTPArrangement]
  val submitArrangementLetterAndControl: LetterAndControl =
    Json.parse(Source.fromFile(s"test/uk/gov/hmrc/timetopay/arrangement/resources/LetterAndControl.json").getLines.mkString).as[LetterAndControl]
  val taxpayer: Taxpayer =
    Json.parse(Source.fromFile(s"test/uk/gov/hmrc/timetopay/arrangement/resources/Taxpayer.json").getLines.mkString).as[Taxpayer]

}
