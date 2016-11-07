package uk.gov.hmrc.timetopay.arrangement.services

import java.time.LocalDate
import java.util.UUID

import play.api.Logger
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.timetopay.arrangement.connectors.ArrangementDesApiConnector.SubmissionResult
import uk.gov.hmrc.timetopay.arrangement.connectors.{SubmissionSuccess, SubmissionError, ArrangementDesApiConnector}
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

  private def saveArrangement(arrangement: TTPArrangement, desSubmissionRequest: DesSubmissionRequest): Future[Option[TTPArrangement]] = {
    val toSave = arrangement.copy(id = Some(UUID.randomUUID().toString),
      createdOn = Some(LocalDate.now()),
      desArrangement = Some(desSubmissionRequest))
    ttpArrangementRepository.save(toSave)
  }

  def submit(arrangement: TTPArrangement)(implicit hc: HeaderCarrier): Future[Option[TTPArrangement]] = {
    Logger.info(s"Submitting ttp arrangement for DD '${arrangement.directDebitReference}' and PP '${arrangement.paymentPlanReference}'")

    val result: Future[SubmissionResult] = for {
      letterAndControl <- letterAndControlService.create(arrangement)
      desTTPArrangement <- desTTPArrangementService.create(arrangement)
      response <- arrangementDesApiConnector.submitArrangement(arrangement.taxpayer,
        DesSubmissionRequest(desTTPArrangement, letterAndControl))
    } yield response

    result.flatMap {
       _.fold(error => Future.failed(new RuntimeException(error.message)),
         success => saveArrangement(arrangement, success.requestSent))
    }
  }

}
