package uk.gov.hmrc.timetopay.arrangement.services

import java.util.UUID

import uk.gov.hmrc.timetopay.arrangement.connectors.ArrangementDesApiConnector
import uk.gov.hmrc.timetopay.arrangement.models.TTPArrangement

import scala.concurrent.Future


object TTPArrangementService extends TTPArrangementService {

  override val arrangementDesApiConnector = ArrangementDesApiConnector

}


trait TTPArrangementService {

  def submit(arrangement: TTPArrangement) = {
    Future.successful(arrangement.copy(identifier = Some(UUID.randomUUID().toString)))
  }


  val arrangementDesApiConnector: ArrangementDesApiConnector

}
