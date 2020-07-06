import scala.util._
import scala.sys.process._

lazy val oldVersion = Try("git describe --abbrev=0".!!.trim.replaceAll("^v", "")).getOrElse("0.2.4")

name := "dijon"

description := "Boiler-free JSON wrangling using Scala dynamic types"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

organization := "com.github.pathikrit"

developers := List(
  Developer(
    id = "pathikrit",
    name = "Pathikrit Bhowmick",
    email = "pathikritbhowmick@msn.com",
    url = new URL(s"http://github.com/pathikrit")
  )
)

resolvers += Resolver.sonatypeRepo("staging")

scalaVersion := "2.13.3"

crossScalaVersions := Seq("2.11.12", "2.12.10", "2.13.3")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:existentials", "-language:dynamics,higherKinds")

libraryDependencies ++= Seq(
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.4.4",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.6",
  "org.scalatest" %% "scalatest" % "3.2.0" % Test
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

  if (isCheckingRequired) Set(organization.value %% moduleName.value % oldVersion)
  else Set()
}
