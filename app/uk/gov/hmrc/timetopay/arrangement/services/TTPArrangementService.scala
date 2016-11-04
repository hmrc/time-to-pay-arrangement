package uk.gov.hmrc.timetopay.arrangement.services

import java.time.LocalDate
import java.util.UUID

import play.api.Logger
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.timetopay.arrangement.connectors.ArrangementDesApiConnector
import uk.gov.hmrc.timetopay.arrangement.models.{DesSubmissionRequest, TTPArrangement}
import uk.gov.hmrc.timetopay.arrangement.repositories.TTPArrangementRepository

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
    Logger.info(s"Submitting ttp arrangement for DD '${arrangement.directDebitReference}' and PP '${arrangement.paymentPlanReference}'")
    val requestFuture = for {
      letterAndControl <- letterAndControlFactory.create(arrangement)
      desTTPArrangement <- desTTPArrangementFactory.create(arrangement)
    } yield DesSubmissionRequest(desTTPArrangement, letterAndControl)

    requestFuture.flatMap {
      request => {
        arrangementDesApiConnector.submitArrangement(arrangement.taxpayer, request).map {
          r => if (r) createResponse(arrangement, request) else throw new RuntimeException("Unable to submit arrangement")
        }
      }
    }


  }

}
