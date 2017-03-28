import sbt._

object MicroServiceBuild extends Build with MicroService {

  val appName = "time-to-pay-arrangement"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.sbt.PlayImport._

  private val scalaTestVersion = "2.2.6"
  private val pegdownVersion = "1.6.0"

  private val playReactivemongoVersion = "4.8.0"
  val compile = Seq(
    "uk.gov.hmrc" %% "play-reactivemongo" % "5.2.0",
    "org.reactivemongo" %% "play2-reactivemongo" % "0.12.0",
    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap" %  "5.14.0",
    "uk.gov.hmrc" %% "play-authorisation" %  "4.3.0",
    "uk.gov.hmrc" %% "play-health" %  "2.1.0",
    "uk.gov.hmrc" %% "play-url-binders" %  "2.1.0",
    "uk.gov.hmrc" %% "play-config" %  "4.2.0",
    "uk.gov.hmrc" %% "logback-json-logger" %  "3.1.0",
    "uk.gov.hmrc" %% "domain" %  "4.1.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1",
        "uk.gov.hmrc" %% "hmrctest" %  "2.3.0",
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % scope,
      "org.mockito" % "mockito-core" % "1.10.19"
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" %  "2.3.0",
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % scope,
        "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % scope,
        "org.mockito" % "mockito-core" % "1.10.19"
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}

