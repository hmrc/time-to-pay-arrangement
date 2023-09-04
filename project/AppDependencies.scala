import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val hmrcMongoVersion = "1.3.0"
  val bootstrapVersion = "7.21.0"

  val jacksonVersion         = "2.13.2"
  val jacksonDatabindVersion = "2.13.2.2"

  val jacksonOverrides = Seq(
    // format: OFF
    "com.fasterxml.jackson.core"     % "jackson-core",
    "com.fasterxml.jackson.core"     % "jackson-annotations",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310"
    // format: ON
  ).map(_ % jacksonVersion)

  val jacksonDatabindOverrides = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonDatabindVersion
  )

  val akkaSerializationJacksonOverrides = Seq(
    // format: OFF
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor",
    "com.fasterxml.jackson.module"     % "jackson-module-parameter-names",
    "com.fasterxml.jackson.module"     %% "jackson-module-scala"
    // format: ON
  ).map(_ % jacksonVersion)

  val compile = Seq(
    // format: OFF
    ws,
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28"         % bootstrapVersion,
    "com.beachape"      %% "enumeratum"                        % "1.7.0",
    "uk.gov.hmrc"       %% "play-scheduling-play-28"           % "8.3.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-work-item-repo-play-28" % hmrcMongoVersion
    // format: ON
  ) ++ jacksonDatabindOverrides ++ jacksonOverrides ++ akkaSerializationJacksonOverrides

  val test = Seq(
    // format: OFF
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % bootstrapVersion,
    "com.vladsch.flexmark"   % "flexmark-all"             % "0.36.8",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % hmrcMongoVersion,
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0",
    "org.pegdown"            % "pegdown"                  % "1.6.0",
    "org.wiremock"           % "wiremock-standalone"      % "3.0.1"
    // format: ON
  ).map(_ % Test)

}
