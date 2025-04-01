import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}

val scalaV = "2.13.16"

val appName = "time-to-pay-arrangement"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    scalaVersion := scalaV,
    resolvers ++= Seq(Resolver.jcenterRepo),
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    retrieveManaged := true,
    routesGenerator := InjectedRoutesGenerator,
    (update / evictionWarningOptions) := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .settings(majorVersion := 0)
  .settings(ScalariformSettings.settings)
  .settings(WartRemoverSettings.settings)
  .settings(ScoverageSettings.settings)
  .settings(SbtUpdatesSettings.sbtUpdatesSettings)
  .settings(PlayKeys.playDefaultPort := 8889)
  .settings(scalaSettings)
  .settings(defaultSettings())
  .settings(scalaVersion := scalaV)
  .settings(
    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-Xlint:-missing-interpolator,_",
      "-Xlint:adapted-args",
      "-Ywarn-unused:implicits",
      "-Ywarn-unused:imports",
      "-Ywarn-unused:locals",
      "-Ywarn-unused:params",
      "-Ywarn-unused:patvars",
      "-Ywarn-unused:privates",
      "-Ywarn-value-discard",
      "-Ywarn-dead-code",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-language:implicitConversions",
      // required in place of silencer plugin
      "-Wconf:cat=unused-imports&src=html/.*:s",
      "-Wconf:src=routes/.*:s"
    )
  )
  .settings(Compile / doc / scalacOptions := Seq())
