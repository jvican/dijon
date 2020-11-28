import scala.util._
import scala.sys.process._

lazy val oldVersion = Try("git describe --abbrev=0".!!.trim.replaceAll("^v", "")).getOrElse("0.2.4")

name := "dijon"

description := "Boiler-free JSON wrangling using Scala dynamic types"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

organization := "me.vican.jorge"

scalaVersion := "2.13.4"

crossScalaVersions := Seq("2.11.12", "2.12.12", "2.13.4")

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-language:existentials",
  "-language:dynamics,higherKinds"
)

libraryDependencies ++= Seq(
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.6.2",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.3.1",
  "org.scalatest" %% "scalatest" % "3.2.3" % Test
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
