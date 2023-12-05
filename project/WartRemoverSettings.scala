import play.sbt.routes.RoutesKeys.routes
import sbt._
import sbt.Keys._
import wartremover.Wart
import wartremover.WartRemover.autoImport.{wartremoverErrors, wartremoverExcluded, wartremoverWarnings}
object  WartRemoverSettings {

  lazy val settings = {
    val warnings = {
      val warningWarts = Seq(
        Wart.JavaSerializable,
        Wart.StringPlusAny,
        Wart.AsInstanceOf,
        Wart.IsInstanceOf
      )
      (Compile / compile / wartremoverWarnings) ++= warningWarts
    }

  val errors = {
    val errorWarts = Seq(
      Wart.ArrayEquals,
      Wart.AnyVal,
      Wart.EitherProjectionPartial,
      Wart.Enumeration,
      Wart.ExplicitImplicitTypes,
      Wart.FinalVal,
      Wart.JavaConversions,
      Wart.JavaSerializable,
      Wart.LeakingSealed,
      Wart.MutableDataStructures,
      Wart.Null,
      Wart.OptionPartial,
      Wart.Recursion,
      Wart.Return,
      Wart.TryPartial,
      Wart.Var,
      Wart.While)

    (Compile / compile / wartremoverErrors) ++= errorWarts
  }

    val excluded = wartremoverExcluded ++=
      (Compile / routes).value ++
        (baseDirectory.value / "it").get ++
        (baseDirectory.value / "test").get ++
        Seq(sourceManaged.value / "main" / "sbt-buildinfo" / "BuildInfo.scala")


    val testExclusions =
      (Test / compile / wartremoverErrors) --= Seq(
        Wart.Any, Wart.Equals, Wart.Null, Wart.NonUnitStatements, Wart.PublicInference
      )


    warnings ++ errors ++ excluded ++ testExclusions
  }

}