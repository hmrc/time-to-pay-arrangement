package uk.gov.hmrc.timetopay.arrangement

import uk.gov.hmrc.timetopay.arrangement.support.{ArrangementActions, IntegrationSpec, TestData}

class GetArrangementDetailsSpec extends IntegrationSpec with ArrangementActions {

  feature("Get Arrangement Details") {

    info("As a consumer of the Arrangement service")
    info("I want to retrieve the arrangement details")
    info("So that I can see what arrangement details are stored")

    scenario("An English user is creating an arrangement") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(TestData.englishHappyJson)
      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      //store identifier in variable
      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement("")
      Then("I should receive a 200 OK response")
      getArrangementGetResponse.status shouldBe OK
      //check enforcement action contains "Distraint"
    }
  }
}
