package uk.gov.hmrc.timetopay.arrangement.support

import org.scalatest.{Ignore, FeatureSpec, GivenWhenThen, Matchers}
import org.scalatest.concurrent.Eventually
import org.scalatestplus.play.OneServerPerSuite

trait IntegrationSpec
  extends FeatureSpec
    with GivenWhenThen
    with OneServerPerSuite
    with Eventually
    with Matchers
    {

    val url = s"http://localhost:$port"
}
