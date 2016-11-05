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
  override val desTTPArrangementService: DesTTPArrangementService = DesTTPArrangementService
  override val letterAndControlService: LetterAndControlService = LetterAndControlService
  override val ttpArrangementRepository: TTPArrangementRepository = TTPArrangementRepository
}


trait TTPArrangementService {

  val arrangementDesApiConnector: ArrangementDesApiConnector
  val desTTPArrangementService: DesTTPArrangementService
  val letterAndControlService: LetterAndControlService
  val ttpArrangementRepository: TTPArrangementRepository


  def byId(id: String): Future[Option[TTPArrangement]] = {
    ttpArrangementRepository.findById(id)
  }

  private def createResponse(arrangement: TTPArrangement, desSubmissionRequest: DesSubmissionRequest): Future[Option[TTPArrangement]] = {
    val toSave = arrangement.copy(id = Some(UUID.randomUUID().toString),
      createdOn = Some(LocalDate.now()),
      desArrangement = Some(desSubmissionRequest))
    ttpArrangementRepository.save(toSave)
  }

  def submit(arrangement: TTPArrangement)(implicit hc: HeaderCarrier): Future[Option[TTPArrangement]] = {
    Logger.info(s"Submitting ttp arrangement for DD '${arrangement.directDebitReference}' and PP '${arrangement.paymentPlanReference}'")

    val result = for {
      letterAndControl <- letterAndControlService.create(arrangement)
      desTTPArrangement <- desTTPArrangementService.create(arrangement)
      request = DesSubmissionRequest(desTTPArrangement, letterAndControl)
      response <- arrangementDesApiConnector.submitArrangement(arrangement.taxpayer, request)
    } yield request -> response

    result.flatMap {
      r => if (r._2) createResponse(arrangement, r._1).map(identity) else Future.failed(new RuntimeException("Unable to submit arrangement"))
    }

  }

}
