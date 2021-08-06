package uk.gov.hmrc.timetopay.arrangement.config


import java.util.concurrent.TimeUnit

import javax.inject.Inject
import play.api.Configuration

import scala.concurrent.duration.{Duration, FiniteDuration}

final case class QueueConfig(retryAfter:String,
                             availableFor:Duration,
                             ttl:Duration,
                             initialDelay:FiniteDuration,
                             interval:FiniteDuration,
                             pollLimit:Int) {


  @Inject()
  def this(configuration: Configuration) {


  this(
    retryAfter = configuration.get[String]("queue.retryAfter"),
    availableFor = configuration.get[Duration]("queue.retryAfter"),
    ttl = configuration.get[Duration]("queue.retryAfter"),
    initialDelay =FiniteDuration(configuration.get[Duration]("queue.retryAfter").toNanos,TimeUnit.NANOSECONDS),
    interval = FiniteDuration(configuration.get[Duration]("queue.retryAfter").toNanos,TimeUnit.NANOSECONDS),
    pollLimit = configuration.get[Int]("queue.pollLimit")
  )
  }
}
