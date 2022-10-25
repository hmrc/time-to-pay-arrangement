import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val hmrcMongoVersion = "0.73.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "5.12.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % hmrcMongoVersion,
    "com.beachape" %% "enumeratum" % "1.5.13",
    "uk.gov.hmrc"  %% "play-scheduling-play-27"   % "7.10.0",
    "uk.gov.hmrc"  %% "crypto"     % "6.0.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-work-item-repo-play-28" % hmrcMongoVersion
  )

  val test = Seq(
    "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-28" % hmrcMongoVersion,
    "org.scalatest" %% "scalatest" % "3.0.9" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
    "org.pegdown" % "pegdown" % "1.6.0",
    "com.github.tomakehurst" % "wiremock-jre8" % "2.21.0",
    "org.mockito" % "mockito-core" % "2.23.0"
  )

}
