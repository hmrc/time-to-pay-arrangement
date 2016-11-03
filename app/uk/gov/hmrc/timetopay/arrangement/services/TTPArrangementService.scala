package uk.gov.hmrc.timetopay.arrangement.services

import java.time.LocalDate
import java.util.UUID

import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.timetopay.arrangement.connectors.ArrangementDesApiConnector
import uk.gov.hmrc.timetopay.arrangement.models.{DesTTPArrangement, DesSubmissionRequest, TTPArrangement}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object TTPArrangementService extends TTPArrangementService {

  override val arrangementDesApiConnector = ArrangementDesApiConnector
  override val desTTPArrangementFactory: DesTTPArrangementService = DesTTPArrangementService
  override val letterAndControlFactory: LetterAndControlService = LetterAndControlService
}


trait TTPArrangementService {

  val arrangementDesApiConnector: ArrangementDesApiConnector
  val desTTPArrangementFactory: DesTTPArrangementService
  val letterAndControlFactory: LetterAndControlService


  def byId(id: String): Future[Option[TTPArrangement]] = {
    throw new NotImplementedError("Yet to be implemented")
  }

  private def createResponse(arrangement: TTPArrangement, desSubmissionRequest: DesSubmissionRequest): TTPArrangement = {
    arrangement.copy(identifier = Some(UUID.randomUUID().toString),
      createdOn = Some(LocalDate.now()),
      desArrangement = Some(desSubmissionRequest))
  }

  def submit(arrangement: TTPArrangement)(implicit hc: HeaderCarrier): Future[TTPArrangement] = {

    val letterAndControl = letterAndControlFactory.create(arrangement)
    val desTTPArrangement = desTTPArrangementFactory.create(arrangement)
    val desSubmissionRequest = DesSubmissionRequest(desTTPArrangement, letterAndControl)

    arrangementDesApiConnector.submitArrangement(arrangement.taxpayer, desSubmissionRequest).map {
      r => if (r) createResponse(arrangement, desSubmissionRequest) else throw new RuntimeException("Unable to submit arrangement")
    }
  }

}
