package uk.gov.hmrc.timetopay.arrangement

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

trait FutureHelpers {
  implicit class futureHelpers[T](f: Future[T]) {
    def waitFor(timeout: FiniteDuration = 10 seconds) = Await.result(f, timeout)
  }
}
