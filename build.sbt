import org.scalajs.linker.interface.{CheckedBehavior, ESVersion}

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

    if (isPatch) "both"
    else "backward"
  },
  mimaPreviousArtifacts := {
    def isCheckingRequired: Boolean = {
      val Array(newMajor, newMinor, _) = version.value.split('.')
      val Array(oldMajor, oldMinor, _) = oldVersion.split('.')
      newMajor == oldMajor && (newMajor != "0" || newMinor == oldMinor)
    }

    if (isCheckingRequired) Set(organization.value %%% moduleName.value % oldVersion)
    else Set()
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

lazy val dijonNative = dijon.native

lazy val dijon = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(
    scalaVersion := "2.13.6", // Update .github/workflows/ci.yml when changing this
    libraryDependencies ++= Seq(
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core" % "2.13.3",
      "org.scala-lang.modules" %%% "scala-collection-compat" % "2.8.1",
      "org.scalatest" %%% "scalatest" % "3.2.16" % Test
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
    scalaJSLinkerConfig ~= {
      _.withSemantics({
        _.optimized
          .withProductionMode(true)
          .withAsInstanceOfs(CheckedBehavior.Unchecked)
          .withArrayIndexOutOfBounds(CheckedBehavior.Unchecked)
      }).withClosureCompiler(true)
        .withESFeatures(_.withESVersion(ESVersion.ES2015))
        .withModuleKind(ModuleKind.CommonJSModule)
    },
    coverageEnabled := false // FIXME: No support for Scala.js 1.0 yet, see https://github.com/scoverage/scalac-scoverage-plugin/pull/287
  )
  .nativeSettings(
    crossScalaVersions := Seq(
      "2.12.13",
      "2.13.6"
    ) // Update .github/workflows/ci.yml when changing this
  )
