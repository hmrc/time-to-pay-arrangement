package uk.gov.hmrc.timetopay.arrangement

import play.api.libs.json.Json

import scala.io.Source

package object itresources {

  val englishHappyRequest = Json.parse(Source.fromFile(s"it/uk/gov/hmrc/timetopay/arrangement/resources/englishHappyRequest.json").getLines.mkString)
  val scottishHappyRequest = Json.parse(Source.fromFile(s"it/uk/gov/hmrc/timetopay/arrangement/resources/scottishHappyRequest.json").getLines.mkString)
  val welshHappyRequest = Json.parse(Source.fromFile(s"it/uk/gov/hmrc/timetopay/arrangement/resources/welshHappyRequest.json").getLines.mkString)


  val englishBadAddressRequest = Json.parse(Source.fromFile(s"it/uk/gov/hmrc/timetopay/arrangement/resources/englishBadAddressRequest.json").getLines.mkString)
  val scottishBadAddressRequest = Json.parse(Source.fromFile(s"it/uk/gov/hmrc/timetopay/arrangement/resources/scottishBadAddressRequest.json").getLines.mkString)
  val welshBadAddressRequest = Json.parse(Source.fromFile(s"it/uk/gov/hmrc/timetopay/arrangement/resources/welshBadAddressRequest.json").getLines.mkString)
  val englishMultipleAddressRequest = Json.parse(Source.fromFile(s"it/uk/gov/hmrc/timetopay/arrangement/resources/englishMultipleAddressRequest.json").getLines.mkString)
  val scottishMultipleAddressRequest = Json.parse(Source.fromFile(s"it/uk/gov/hmrc/timetopay/arrangement/resources/scottishMultipleAddressRequest.json").getLines.mkString)
  val welshMultipleAddressRequest = Json.parse(Source.fromFile(s"it/uk/gov/hmrc/timetopay/arrangement/resources/welshMultipleAddressRequest.json").getLines.mkString)
  val englishNoAddressRequest = Json.parse(Source.fromFile(s"it/uk/gov/hmrc/timetopay/arrangement/resources/englishNoAddressRequest.json").getLines.mkString)


  val welshPreferenceRequest = Json.parse(Source.fromFile(s"it/uk/gov/hmrc/timetopay/arrangement/resources/welshPreferenceRequest.json").getLines.mkString)
  val welshPreferenceLargePrintRequest = Json.parse(Source.fromFile(s"it/uk/gov/hmrc/timetopay/arrangement/resources/welshPreferenceLargePrintRequest.json").getLines.mkString)
  val englishAudioIndicatorRequest = Json.parse(Source.fromFile(s"it/uk/gov/hmrc/timetopay/arrangement/resources/englishAudioIndicatorRequest.json").getLines.mkString)
  val englishLargePrintRequest = Json.parse(Source.fromFile(s"it/uk/gov/hmrc/timetopay/arrangement/resources/englishLargePrintRequest.json").getLines.mkString)
  val englishBrailleRequest = Json.parse(Source.fromFile(s"it/uk/gov/hmrc/timetopay/arrangement/resources/englishBrailleRequest.json").getLines.mkString)




}
