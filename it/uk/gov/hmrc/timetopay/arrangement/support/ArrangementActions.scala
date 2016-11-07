package uk.gov.hmrc.timetopay.arrangement.support

import play.api.libs.ws.{WS, WSResponse}
import play.api.Play.current
import play.api.libs.json.JsValue


trait ArrangementActions extends ActionsSupport {

  def getArrangement(url: String): WSResponse =
    WS
      .url(url)
      .get()
      .futureValue

//  def postArrangements(data: Json): WSResponse = postArrangements(data.getBytes())

  def postArrangements(url: String, data: JsValue): WSResponse =
    WS
      .url(s"$url/ttparrangements")
      .withHeaders("Content-Type" -> "application/json")
      .post(data)
      .futureValue
}

