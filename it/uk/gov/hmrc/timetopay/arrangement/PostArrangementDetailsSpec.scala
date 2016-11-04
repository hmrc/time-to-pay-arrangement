package uk.gov.hmrc.timetopay.arrangement

import play.api.libs.json.Json
import uk.gov.hmrc.timetopay.arrangement.support.{ArrangementActions, IntegrationSpec, TestData}

/**
  * Integration tests for SSTTP-364
  * POST /ttparrangements
  */

class PostArrangementDetailsSpec extends IntegrationSpec with ArrangementActions {

  feature("Post Arrangement Details") {

    info("As a consumer of the Arrangement service")
    info("I want to be able to create a new arrangement")

    scenario("An English user is creating an arrangement") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(TestData.englishHappyJson)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      println("Response status: " + getArrangementPostResponse.status) //DEBUG
      val locationHeader = getArrangementPostResponse.header("LOCATION").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)
      println("Location content: " + locationHeader) //DEBUG

      Then("I should receive a 200 OK response")
      getArrangementGetResponse.status shouldBe OK
      println(getArrangementGetResponse.json \ "enforcementAction") //DEBUG
      getArrangementGetResponse.json \ "enforcementAction" shouldBe "Distraint" //Not sure if this works, if not try the next one
    }

    scenario("A Scottish user is creating an arrangement") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(TestData.scottishHappyJson)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("LOCATION").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response")
      getArrangementGetResponse.status shouldBe OK
      val body = Json.parse(getArrangementGetResponse.body)
      body \ "enforcementAction" shouldBe "Summary Warrant"
    }

    scenario("A Welsh user is creating an arrangement") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(TestData.welshHappyJson)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("LOCATION").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response")
      getArrangementGetResponse.status shouldBe OK
      getArrangementGetResponse.json \ "enforcementAction" shouldBe "Distraint"
    }
  }
}
