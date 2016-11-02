package uk.gov.hmrc.timetopay.arrangement.services

import java.util.UUID

import uk.gov.hmrc.timetopay.arrangement.connectors.ArrangementDesApiConnector
import uk.gov.hmrc.timetopay.arrangement.models.TTPArrangement

import scala.concurrent.Future


object TTPArrangementService extends TTPArrangementService {

  override val arrangementDesApiConnector = ArrangementDesApiConnector

}


trait TTPArrangementService {
  def byId(id: String): Future[Option[TTPArrangement]] = {
     Future.successful(None)
  }


  val arrangementDesApiConnector: ArrangementDesApiConnector

  def submit(arrangement: TTPArrangement) = {
    Future.successful(arrangement.copy(identifier = Some(UUID.randomUUID().toString)))
  }

}
