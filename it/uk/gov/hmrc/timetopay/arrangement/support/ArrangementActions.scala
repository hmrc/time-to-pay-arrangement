package uk.gov.hmrc.timetopay.arrangement.support

import play.api.libs.ws.{WS, WSResponse}
import play.api.Play.current


trait ArrangementActions extends ActionsSupport {

  def getArrangement(url: String): WSResponse =
    WS
      .url(url)
      .get()
      .futureValue

  def postArrangements(data: String): WSResponse = postArrangements(data.getBytes())

  def postArrangements(data: Array[Byte]): WSResponse =
    WS
      .url(s"$url/ttparrangements")
      .withHeaders("Content-Type" -> "application/json")
      .post(data)
      .futureValue
}

