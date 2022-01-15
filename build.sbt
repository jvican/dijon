import scala.util._
import scala.sys.process._

lazy val oldVersion = Try("git describe --abbrev=0".!!.trim.replaceAll("^v", "")).getOrElse("0.2.4")

lazy val commonSettings = Seq(
  name := "dijon",
  description := "Boiler-free JSON wrangling using Scala dynamic types",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  organization := "me.vican.jorge",
  homepage := Some(url("https://github.com/jvican/dijon")),
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/jvican/dijon"),
      "scm:git:git@github.com:jvican/dijon.git"
    )
  ),
  developers := List(
    Developer(
      "pathikrit",
      "Pathikrit Bhowmick",
      "pathikritbhowmick@msn.com",
      new URL(s"http://github.com/pathikrit")
    ),
    Developer(
      "jvican",
      "Jorge Vicente Cantero",
      "jorgevc@fastmail.es",
      url("https://jvican.github.io/")
    )
  ),
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:existentials",
    "-language:dynamics,higherKinds"
  ),
  publishTo := sonatypePublishToBundle.value,
  releaseEarlyWith := SonatypePublisher
)

lazy val publishSettings = Seq(
  packageOptions += Package.ManifestAttributes("Automatic-Module-Name" -> moduleName.value),
  mimaCheckDirection := {
    def isPatch: Boolean = {
      val Array(newMajor, newMinor, _) = version.value.split('.')
      val Array(oldMajor, oldMinor, _) = oldVersion.split('.')
      newMajor == oldMajor && newMinor == oldMinor
    }

    if (isPatch) "both" else "backward"
  },
  mimaPreviousArtifacts := {
    def isCheckingRequired: Boolean = {
      val Array(newMajor, newMinor, _) = version.value.split('.')
      val Array(oldMajor, oldMinor, _) = oldVersion.split('.')
      newMajor == oldMajor && (newMajor != "0" || newMinor == oldMinor)
    }

    if (isCheckingRequired && (oldVersion != "0.5.0" || scalaVersion.value == "2.13.6")) {
      Set(organization.value %%% moduleName.value % oldVersion)
    } else Set()
  }
)

lazy val noPublishSettings = Seq(
  mimaPreviousArtifacts := Set(),
  publish / skip := true
)

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(noPublishSettings)
  .aggregate(dijonJVM, dijonJS)

lazy val dijonJVM = dijon.jvm

lazy val dijonJS = dijon.js

lazy val dijon = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(
    scalaVersion := "2.13.6", // Update .github/workflows/ci.yml when changing this
    libraryDependencies ++= Seq(
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core" % "2.12.1",
      "org.scala-lang.modules" %%% "scala-collection-compat" % "2.6.0",
      "org.scalatest" %%% "scalatest" % "3.2.10" % Test
    )
  )
  .jvmSettings(
    crossScalaVersions := Seq(
      "2.11.12",
      "2.12.13",
      "2.13.6"
    ) // Update .github/workflows/ci.yml when changing this
  )
  .jsSettings(
    crossScalaVersions := Seq(
      "2.11.12",
      "2.12.13",
      "2.13.6"
    ), // Update .github/workflows/ci.yml when changing this
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)
      .withESFeatures(_.withUseECMAScript2015(false))),
    coverageEnabled := false // FIXME: No support for Scala.js 1.0 yet, see https://github.com/scoverage/scalac-scoverage-plugin/pull/287
  )
