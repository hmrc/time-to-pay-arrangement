
import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  val hmrcMongoVersion = "2.3.0"
  val bootstrapVersion = "9.5.0"


  val compile = Seq(
    // format: OFF
    ws,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"         % bootstrapVersion,
    "com.beachape"      %% "enumeratum"                        % "1.7.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-work-item-repo-play-30" % hmrcMongoVersion
    // format: ON
  )

  val test = Seq(
    // format: OFF
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatestplus.play" %% "scalatestplus-play"      % "7.0.1",
    "org.wiremock"           %  "wiremock-standalone"     % "3.10.0",
    "com.github.pjfanning"   %% "pekko-mock-scheduler"    % "0.6.0"
    // format: ON
  ).map(_ % Test)

}
