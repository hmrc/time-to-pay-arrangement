package uk.gov.hmrc.timetopay.arrangement

import uk.gov.hmrc.timetopay.arrangement.support.{ArrangementActions, IntegrationSpec, TestData}

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
      val getArrangementPostResponse = postArrangements(TestData.welshPreferenceJson)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("Location").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response and the JSON values should be correct")
      getArrangementGetResponse.status shouldBe OK
      getArrangementGetResponse.json \ "welshLanguageIndicator" shouldBe "true"
      getArrangementGetResponse.json \ "exceptionReason" should include regex "welsh" //check with devs as to the wording
    }

    scenario("A Welsh user is creating an arrangement with Welsh communication preference") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(TestData.welshPreferenceLargePrintJson)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("Location").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response and the JSON values should be correct")
      getArrangementGetResponse.status shouldBe OK
      getArrangementGetResponse.json \ "welshLanguageIndicator" shouldBe "true"
      getArrangementGetResponse.json \ "largePrintIndicator" shouldBe "true"
      getArrangementGetResponse.json \ "exceptionReason" should include regex "welsh" //check with devs as to the wording
    }

    scenario("An English user is creating an arrangement with audio indicator communication preference") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(TestData.englishAudioIndicatorJson)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("LOCATION").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response and the JSON values should be correct")
      getArrangementGetResponse.status shouldBe OK
      getArrangementGetResponse.json \ "audioIndicator" shouldBe "true"
      getArrangementGetResponse.json \ "exceptionReason" should include regex "audio" //check with devs as to the wording
    }

    scenario("An English user is creating an arrangement with large print communication preference") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(TestData.englishLargePrintJson)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("LOCATION").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response and the JSON values should be correct")
      getArrangementGetResponse.status shouldBe OK
      getArrangementGetResponse.json \ "largePrintIndicator" shouldBe "true"
      getArrangementGetResponse.json \ "exceptionReason" should include regex "Large Format printing" //check with devs as to the wording
    }

    scenario("An English user is creating an arrangement with large print communication preference") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(TestData.englishBrailleJson)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("LOCATION").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response and the JSON values should be correct")
      getArrangementGetResponse.status shouldBe OK
      getArrangementGetResponse.json \ "brailleIndicator" shouldBe "true"
      getArrangementGetResponse.json \ "exceptionReason" should include regex "braille" //check with devs as to the wording
    }
  }
}
