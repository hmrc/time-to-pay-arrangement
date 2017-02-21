package uk.gov.hmrc.timetopay.arrangement.services

import akka.actor.Actor.Receive
import akka.actor._
import akka.stream.ActorMaterializer
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.timetopay.arrangement.{DesSubmissionRequest, Taxpayer}
import play.api.http.Status._

import scala.concurrent.{ExecutionContext, Future}

class DesArrangementActor(taxpayer: Taxpayer, desSubmissionRequest: DesSubmissionRequest, initialSubmissionCount: Int = 0) extends Actor {
  private val system: ActorSystem = ActorSystem()
  private val materializer = ActorMaterializer()
  private val executor = system.dispatcher

  type SubmissionResult = Either[SubmissionError, SubmissionSuccess]

  var submissionCount = 0

  def receive: Receive = {
    case HttpResponse(BAD_REQUEST, body, _, _) if body.toString().contains("Your submission contains one or more errors") && submissionCount == 1 =>
    submissionCount = submissionCount + 1

    //resubmit
    case HttpResponse(OK, body, _, _) => Right(SubmissionSuccess)
    case HttpResponse(code, body, _, _) => Left(SubmissionError(code, body.toString()))
  }
  //Increase submission count
  //Successful submission, return happy response
  //error in submission NOT validation,return failure response
  //if 1st attempt failed on validation error, try again
    //remove letter and control
  //if 2nd attempt failed on validation error, return failure response

  def sendToDes = ???
  //just send to DES

  def submitArrangement(taxpayer: Taxpayer, desSubmissionRequest: DesSubmissionRequest)(implicit ec: ExecutionContext): Future[SubmissionResult] = ???
  //reset submission count
  //Submission Request to DES
  //return response
}

/*
def submitArrangement(taxpayer: Taxpayer, desSubmissionRequest: DesSubmissionRequest)(implicit ec: ExecutionContext): Future[SubmissionResult] = {
    implicit val hc: HeaderCarrier = desHeaderCarrier

    val serviceUrl = s"time-to-pay/taxpayers/${taxpayer.selfAssessment.utr}/arrangements"

    Logger.debug(s"Header carrier ${hc.headers}")

    http.POST[DesSubmissionRequest, HttpResponse](s"$desArrangementUrl/$serviceUrl", desSubmissionRequest)
      .map(_ => {
        Logger.info(s"Submission successful for '${taxpayer.selfAssessment.utr}'")
        Right(SubmissionSuccess())
      }).recover {
      case e: Throwable => onError(e)
    }
  }
 */
