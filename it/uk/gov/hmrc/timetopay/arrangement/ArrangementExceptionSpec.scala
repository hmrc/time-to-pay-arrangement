package uk.gov.hmrc.timetopay.arrangement

import org.scalatest.Ignore
import uk.gov.hmrc.timetopay.arrangement.itresources._
import uk.gov.hmrc.timetopay.arrangement.support.{ArrangementActions, IntegrationSpec}

/**
  * Integration tests for SSTTP-364
  * POST /ttparrangements
  */


class ArrangementExceptionSpec extends IntegrationSpec with ArrangementActions {

  feature("Post Arrangement Details with an exception") {

    info("As a consumer of the Arrangement service")
    info("I want to be able to create a new arrangement")

    ignore("An English user is creating an arrangement with a bad address") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(url, englishBadAddressRequest)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("LOCATION").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response and the exceptionReason shall include the correct value")
      getArrangementGetResponse.status shouldBe OK
      val exceptionReason = (getArrangementGetResponse.json \ "letterAndControl" \ "exceptionReason").as[String]
      exceptionReason should include ("Bad Address")
    }

    //Can't use this test, bad address is first line and postcode missing, unable tell user is Scottish without a postcode being provided
//    ignore("A Scottish user is creating an arrangement with a bad address") {
//      When("I call POST /ttparrangements")
//      val getArrangementPostResponse = postArrangements(url, scottishBadAddressRequest)
//
//      Then("I should receive a 201 CREATED response")
//      getArrangementPostResponse.status shouldBe CREATED
//      val locationHeader = getArrangementPostResponse.header("LOCATION").get
//
//      When("I call GET /ttparrangements/{arrangement-identifier")
//      val getArrangementGetResponse = getArrangement(locationHeader)
//
//      Then("I should receive a 200 OK response and the exceptionReason shall include the correct value")
//      getArrangementGetResponse.status shouldBe OK
//      val exceptionReason = (getArrangementGetResponse.json \ "letterAndControl" \ "exceptionReason").as[String]
//      exceptionReason should include ("Bad Address")
//    }

    //Can't use this test, bad address is first line and postcode missing, unable tell user is Welsh without a postcode being provided
//    ignore("A Welsh user is creating an arrangement with a bad address") {
//      When("I call POST /ttparrangements")
//      val getArrangementPostResponse = postArrangements(url, welshBadAddressRequest)
//
//      Then("I should receive a 201 CREATED response")
//      getArrangementPostResponse.status shouldBe CREATED
//      val locationHeader = getArrangementPostResponse.header("LOCATION").get
//
//      When("I call GET /ttparrangements/{arrangement-identifier")
//      val getArrangementGetResponse = getArrangement(locationHeader)
//
//      Then("I should receive a 200 OK response and the exceptionReason shall include the correct value")
//      getArrangementGetResponse.status shouldBe OK
//      val exceptionReason = (getArrangementGetResponse.json \ "letterAndControl" \ "exceptionReason").as[String]
//      exceptionReason should include ("Bad Address")
//    }

    ignore("An English user is creating an arrangement with multiple addresses") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(url, englishMultipleAddressRequest)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("LOCATION").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response and the exceptionReason shall include the correct value")
      getArrangementGetResponse.status shouldBe OK
      val exceptionReason = (getArrangementGetResponse.json \ "letterAndControl" \ "exceptionReason").as[String]
      exceptionReason should include ("Multiple Address")
    }

    ignore("A Scottish user is creating an arrangement with multiple addresses") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(url, scottishMultipleAddressRequest)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("LOCATION").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response and the exceptionReason shall include the correct value")
      getArrangementGetResponse.status shouldBe OK
      val exceptionReason = (getArrangementGetResponse.json \ "letterAndControl" \ "exceptionReason").as[String]
      exceptionReason should include ("Multiple Address")
    }

    ignore("A Welsh user is creating an arrangement with a multiple addresses") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(url, welshMultipleAddressRequest)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("LOCATION").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response and the exceptionReason shall include the correct value")
      getArrangementGetResponse.status shouldBe OK
      val exceptionReason = (getArrangementGetResponse.json \ "letterAndControl" \ "exceptionReason").as[String]
      exceptionReason should include regex "Multiple Address"
    }

    ignore("An English user is creating an arrangement with no address") {
      When("I call POST /ttparrangements")
      val getArrangementPostResponse = postArrangements(url, englishNoAddressRequest)

      Then("I should receive a 201 CREATED response")
      getArrangementPostResponse.status shouldBe CREATED
      val locationHeader = getArrangementPostResponse.header("LOCATION").get

      When("I call GET /ttparrangements/{arrangement-identifier")
      val getArrangementGetResponse = getArrangement(locationHeader)

      Then("I should receive a 200 OK response and the exceptionReason shall include the correct value")
      getArrangementGetResponse.status shouldBe OK
      print(getArrangementGetResponse.body)
      val exceptionReason = (getArrangementGetResponse.json \ "letterAndControl" \ "exceptionReason").as[String]
      exceptionReason should include ("No Address")
    }

    //may not be able to test, unable to tell if a Scottish user is using the system if no address is entered
//    ignore("A Scottish user is creating an arrangement with no address") {
//      When("I call POST /ttparrangements")
//      val getArrangementPostResponse = postArrangements(url, scottishNoAddressRequest)
//
//      Then("I should receive a 201 CREATED response")
//      getArrangementPostResponse.status shouldBe CREATED
//      val locationHeader = getArrangementPostResponse.header("LOCATION").get
//
//      When("I call GET /ttparrangements/{arrangement-identifier")
//      val getArrangementGetResponse = getArrangement(locationHeader)
//
//      Then("I should receive a 200 OK response and the exceptionReason shall include the correct value")
//      getArrangementGetResponse.status shouldBe OK
//      val exceptionReason = (getArrangementGetResponse.json \ "letterAndControl" \ "exceptionReason").as[String]
//      exceptionReason should include ("No Address")
//    }

    //may not be able to test, unable to tell if a Welsh user is using the system if no address is entered
//    ignore("A Welsh user is creating an arrangement with no address") {
//      When("I call POST /ttparrangements")
//      val getArrangementPostResponse = postArrangements(url, welshNoAddressRequest)
//
//      Then("I should receive a 201 CREATED response")
//      getArrangementPostResponse.status shouldBe CREATED
//      val locationHeader = getArrangementPostResponse.header("LOCATION").get
//
//      When("I call GET /ttparrangements/{arrangement-identifier")
//      val getArrangementGetResponse = getArrangement(locationHeader)
//
//      Then("I should receive a 200 OK response and the exceptionReason shall include the correct value")
//      getArrangementGetResponse.status shouldBe OK
//      val exceptionReason = (getArrangementGetResponse.json \ "letterAndControl" \ "exceptionReason").as[String]
//      exceptionReason should include ("No Address")
//    }
  }
}
