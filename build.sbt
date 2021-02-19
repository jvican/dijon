import scala.util._
import scala.sys.process._

lazy val oldVersion = Try("git describe --abbrev=0".!!.trim.replaceAll("^v", "")).getOrElse("0.2.4")

name := "dijon"

description := "Boiler-free JSON wrangling using Scala dynamic types"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

organization := "me.vican.jorge"

// Update .github/workflows/ci.yml when changing this
scalaVersion := "2.13.4"
// Update .github/workflows/ci.yml when changing this
crossScalaVersions := Seq("2.11.12", "2.12.12", "2.13.4")

homepage := Some(url("https://github.com/jvican/dijon"))
licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/jvican/dijon"),
    "scm:git:git@github.com:jvican/dijon.git"
  )
)

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
)

releaseEarlyWith := SonatypePublisher
publishTo := sonatypePublishToBundle.value

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-language:existentials",
  "-language:dynamics,higherKinds"
)

libraryDependencies ++= Seq(
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.6.4",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.4.2",
  "org.scalatest" %% "scalatest" % "3.2.5" % Test
)

mimaCheckDirection := {
  def isPatch: Boolean = {
    val Array(newMajor, newMinor, _) = version.value.split('.')
    val Array(oldMajor, oldMinor, _) = oldVersion.split('.')
    newMajor == oldMajor && newMinor == oldMinor
  }

  if (isPatch) "both" else "backward"
}

mimaPreviousArtifacts := {
  def isCheckingRequired: Boolean = {
    val Array(newMajor, newMinor, _) = version.value.split('.')
    val Array(oldMajor, oldMinor, _) = oldVersion.split('.')
    newMajor == oldMajor && (newMajor != "0" || newMinor == oldMinor)
  }

  if (oldVersion == "0.3.0") Set()
  else if (isCheckingRequired) Set(organization.value %% moduleName.value % oldVersion)
  else Set()
}
