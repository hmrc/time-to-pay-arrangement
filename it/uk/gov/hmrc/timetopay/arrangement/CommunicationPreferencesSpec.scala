package uk.gov.hmrc.timetopay.arrangement

import uk.gov.hmrc.timetopay.arrangement.itresources._
import uk.gov.hmrc.timetopay.arrangement.support.{ArrangementActions, IntegrationSpec}

/**
  * Integration tests for SSTTP-364
  * POST /ttparrangements
  */

class CommunicationPreferencesSpec extends IntegrationSpec with ArrangementActions {

  feature("Post Arrangement Details with communication preferences") {

    info("As a consumer of the Arrangement service")
    info("I want to be able to create a new arrangement")

    scenario("A Welsh user is creating an arrangement with Welsh communication preference") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(welshPreferenceRequest)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("Location").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response and the JSON values should be correct")
      getArrangementGetResponse.status shouldBe OK
      val exceptionReason = (getArrangementGetResponse.json \ "letterAndControl" \ "exceptionReason").as[String]
      exceptionReason should include regex "Welsh Preference"
    }

    scenario("A Welsh user is creating an arrangement with Welsh communication preference") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(welshPreferenceLargePrintRequest)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("Location").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response and the JSON values should be correct")
      getArrangementGetResponse.status shouldBe OK
      val exceptionReason = (getArrangementGetResponse.json \ "letterAndControl" \ "exceptionReason").as[String]
      exceptionReason should include regex "Welsh Preference Large Print"
    }

    scenario("An English user is creating an arrangement with audio indicator communication preference") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(englishAudioIndicatorRequest)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("LOCATION").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response and the JSON values should be correct")
      getArrangementGetResponse.status shouldBe OK
      val exceptionReason = (getArrangementGetResponse.json \ "letterAndControl" \ "exceptionReason").as[String]
      exceptionReason should include regex "Audio Indicator"
    }

    scenario("An English user is creating an arrangement with large print communication preference") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(englishLargePrintRequest)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("LOCATION").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response and the JSON values should be correct")
      getArrangementGetResponse.status shouldBe OK
      val exceptionReason = (getArrangementGetResponse.json \ "letterAndControl" \ "exceptionReason").as[String]
      exceptionReason should include regex "Large Print"
    }

    scenario("An English user is creating an arrangement with large print communication preference") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(englishBrailleRequest)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("LOCATION").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response and the JSON values should be correct")
      getArrangementGetResponse.status shouldBe OK
      val exceptionReason = (getArrangementGetResponse.json \ "letterAndControl" \ "exceptionReason").as[String]
      exceptionReason should include regex "Braille"
    }
  }
}
