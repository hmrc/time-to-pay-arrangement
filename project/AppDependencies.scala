import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val hmrcMongoVersion = "1.6.0"
  val bootstrapVersion = "8.1.0"


  val compile = Seq(
    // format: OFF
    ws,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28"         % bootstrapVersion,
    "com.beachape"      %% "enumeratum"                        % "1.7.0",
    "uk.gov.hmrc"       %% "play-scheduling-play-28"           % "8.3.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-work-item-repo-play-28" % hmrcMongoVersion
    // format: ON
  )

  val test = Seq(
    // format: OFF
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % bootstrapVersion,
    "com.vladsch.flexmark"   % "flexmark-all"             % "0.36.8",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % hmrcMongoVersion,
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0",
    "org.pegdown"            % "pegdown"                  % "1.6.0",
    "org.wiremock"           % "wiremock-standalone"      % "3.3.1"
    // format: ON
  ).map(_ % Test)

}
