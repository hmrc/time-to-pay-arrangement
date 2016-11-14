package uk.gov.hmrc.timetopay.arrangement.services

import java.time.{LocalDateTime, LocalDate}
import java.util.UUID

import play.api.Logger
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.timetopay.arrangement.connectors.{ArrangementDesApiConnector}
import uk.gov.hmrc.timetopay.arrangement.models.{LetterAndControl, DesTTPArrangement, DesSubmissionRequest, TTPArrangement}
import uk.gov.hmrc.timetopay.arrangement.repositories.TTPArrangementRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TTPArrangementService(arrangementDesApiConnector: ArrangementDesApiConnector,
                            desTTPArrangementService: (TTPArrangement => Future[DesTTPArrangement]),
                            letterAndControlService: (TTPArrangement => Future[LetterAndControl]),
                            arrangementSave: (TTPArrangement => Future[Option[TTPArrangement]]),
                            arrangementGet: (String => Future[Option[TTPArrangement]])) {

  def byId(id: String): Future[Option[TTPArrangement]] = arrangementGet(id)

  def submit(arrangement: TTPArrangement)(implicit hc: HeaderCarrier): Future[Option[TTPArrangement]] = {
    Logger.info(s"Submitting ttp arrangement for DD '${arrangement.directDebitReference}' and PP '${arrangement.paymentPlanReference}'")

    (for {
      letterAndControl <- letterAndControlService(arrangement)
      desTTPArrangement <- desTTPArrangementService(arrangement)
      response <- arrangementDesApiConnector.submitArrangement(arrangement.taxpayer,
        DesSubmissionRequest(desTTPArrangement, letterAndControl))
    } yield response).flatMap {
       _.fold(error => Future.failed(new RuntimeException(error.message)),
         success => saveArrangement(arrangement, success.requestSent))
    }
  }

  private def saveArrangement(arrangement: TTPArrangement, desSubmissionRequest: DesSubmissionRequest): Future[Option[TTPArrangement]] = {
    val toSave = arrangement.copy(id = Some(UUID.randomUUID().toString),
      createdOn = Some(LocalDateTime.now()),
      desArrangement = Some(desSubmissionRequest))
      arrangementSave(toSave)
  }


}
