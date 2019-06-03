import sbt._

object MicroServiceBuild extends Build with MicroService {

  val appName = "time-to-pay-arrangement"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.sbt.PlayImport._

  private val scalaTestVersion = "3.0.5"
  private val pegdownVersion = "1.6.0"
  private val scalatestplusPlayVersion = "2.0.1"
  private val scalamockScalatestSupportVersion = "3.6.0"
  
  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "play-reactivemongo" % "6.2.0",
    "org.reactivemongo" %% "play2-reactivemongo" % "0.12.0",
    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap" %  "9.1.0",
    "uk.gov.hmrc" %% "play-ui" % "7.27.0-play-25",
    "uk.gov.hmrc" %% "domain" %  "4.1.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test: Seq[ModuleID] = Seq(
        "org.scalatestplus.play" %% "scalatestplus-play" % scalatestplusPlayVersion,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.scalamock" %% "scalamock-scalatest-support" % scalamockScalatestSupportVersion % scope,
        "org.mockito" % "mockito-core" % "2.18.3" % "test,it"
      )
    }.test
  }

  object IntegrationTest {
    def apply(): Seq[ModuleID] = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test: Seq[ModuleID] = Seq(
        "uk.gov.hmrc" %% "hmrctest" %  "3.3.0",
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalatestplusPlayVersion % scope,
        "org.scalamock" %% "scalamock-scalatest-support" % scalamockScalatestSupportVersion % scope,
        "org.mockito" % "mockito-core" % "1.10.19"
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()
}

