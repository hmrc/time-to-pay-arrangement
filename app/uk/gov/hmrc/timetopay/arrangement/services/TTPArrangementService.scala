package uk.gov.hmrc.timetopay.arrangement.services

import java.time.LocalDateTime
import java.util.UUID

import play.api.Logger
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.timetopay.arrangement.connectors.{SubmissionError, SubmissionSuccess}
import uk.gov.hmrc.timetopay.arrangement.models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TTPArrangementService(arrangementDesApiConnector: ((Taxpayer, DesSubmissionRequest) => Future[Either[SubmissionError, SubmissionSuccess]]),
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
      response <- arrangementDesApiConnector(arrangement.taxpayer,DesSubmissionRequest(desTTPArrangement, letterAndControl))
    } yield response).flatMap {
       _.fold(error => Future.failed(DesApiException(error.code, error.message)),
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

case class DesApiException(code: Int, message: String) extends RuntimeException(s"DES httpCode: $code, reason: $message") {}
