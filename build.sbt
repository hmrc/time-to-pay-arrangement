import TestPhases.oneForkedJvmPerTest
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings, scalaSettings}
import wartremover.Wart
import wartremover.WartRemover.autoImport.{wartremoverErrors, wartremoverExcluded, wartremoverWarnings}

val scalaV = "2.13.11"

val appName = "time-to-pay-arrangement"

lazy val scalariformSettings = {
  // description of options found here -> https://github.com/scala-ide/scalariform
  ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(AlignArguments, true)
    .setPreference(AlignParameters, true)
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(AllowParamGroupsOnNewlines, true)
    .setPreference(CompactControlReadability, false)
    .setPreference(CompactStringConcatenation, false)
    .setPreference(DanglingCloseParenthesis, Preserve)
    .setPreference(DoubleIndentConstructorArguments, true)
    .setPreference(DoubleIndentMethodDeclaration, true)
    .setPreference(FirstArgumentOnNewline, Preserve)
    .setPreference(FirstParameterOnNewline, Preserve)
    .setPreference(FormatXml, true)
    .setPreference(IndentLocalDefs, true)
    .setPreference(IndentPackageBlocks, true)
    .setPreference(IndentSpaces, 2)
    .setPreference(IndentWithTabs, false)
    .setPreference(MultilineScaladocCommentsStartOnFirstLine, false)
    .setPreference(NewlineAtEndOfFile, true)
    .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, false)
    .setPreference(PreserveSpaceBeforeArguments, true)
    .setPreference(RewriteArrowSymbols, false)
    .setPreference(SpaceBeforeColon, false)
    .setPreference(SpaceBeforeContextColon, false)
    .setPreference(SpaceInsideBrackets, false)
    .setPreference(SpaceInsideParentheses, false)
    .setPreference(SpacesAroundMultiImports, false)
    .setPreference(SpacesWithinPatternBinders, true)
}
lazy val wartRemoverWarning = {
  val warningWarts = Seq(
    Wart.JavaSerializable,
    Wart.StringPlusAny,
    Wart.AsInstanceOf,
    Wart.IsInstanceOf
    //Wart.Any
  )
  (Compile / compile / wartremoverWarnings) ++= warningWarts
}
lazy val wartRemoverError = {
  // Error
  val errorWarts = Seq(
    Wart.ArrayEquals,
    Wart.AnyVal,
    Wart.EitherProjectionPartial,
    Wart.Enumeration,
    Wart.ExplicitImplicitTypes,
    Wart.FinalVal,
    Wart.JavaConversions,
    Wart.JavaSerializable,
    //Wart.LeakingSealed,
    Wart.MutableDataStructures,
    Wart.Null,
    //Wart.OptionPartial,
    Wart.Recursion,
    Wart.Return,
    //Wart.TryPartial,
    Wart.Var,
    Wart.While)

  (Compile / compile / wartremoverErrors) ++= errorWarts
}
lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;.*BuildInfo.*;Reverse.*;app.Routes.*;prod.*;testOnlyDoNotUseInAppConf.*;forms.*;config.*;uk.gov.hmrc.timetopay.arrangement.config.*",
    ScoverageKeys.coverageExcludedFiles := ".*microserviceGlobal.*;.*microserviceWiring.*",
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}
lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
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
  .settings(scalariformSettings: _*)
  .settings(wartRemoverError)
  .settings(wartRemoverWarning)
  .settings((Test / compile / wartremoverErrors) --= Seq(Wart.Any, Wart.Equals, Wart.Null, Wart.NonUnitStatements, Wart.PublicInference))
  .settings(wartremoverExcluded ++=
    (Compile / routes).value ++
      (baseDirectory.value / "it").get ++
      (baseDirectory.value / "test").get ++
      Seq(sourceManaged.value / "main" / "sbt-buildinfo" / "BuildInfo.scala"))
  .settings(scoverageSettings: _*)
  .settings(SbtUpdatesSettings.sbtUpdatesSettings: _*)
  .settings(PlayKeys.playDefaultPort := 8889)
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(scalaVersion := scalaV)
  .settings(integrationTestSettings())
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    (IntegrationTest / Keys.fork) := false,
    (IntegrationTest / unmanagedSourceDirectories) := (IntegrationTest / baseDirectory) (base => Seq(base / "it")).value,
    (IntegrationTest / testGrouping) := oneForkedJvmPerTest((IntegrationTest / definedTests).value),
    (IntegrationTest / parallelExecution) := false)
  .settings(
    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-Ywarn-dead-code",
      "-Ywarn-unused",
      "-Ywarn-value-discard",
      "-Ywarn-unused:-imports",
      "-unchecked",
      "-feature"
    )
  )
  .settings(Compile / doc / scalacOptions := Seq())
