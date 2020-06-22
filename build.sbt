import scala.util._
import scala.sys.process._

lazy val oldVersion = Try("git describe --abbrev=0".!!.trim.replaceAll("^v", "")).getOrElse("0.2.4")

name := "dijon"

description := "Boiler-free JSON wrangling using Scala dynamic types"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

organization := "com.github.pathikrit"

resolvers += Resolver.sonatypeRepo("staging")

scalaVersion := "2.13.2"

crossScalaVersions := Seq("2.11.12", "2.12.10", "2.13.2")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:existentials", "-language:dynamics,higherKinds")

libraryDependencies ++= Seq(
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.4.3",
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

publishTo := sonatypePublishToBundle.value

publishMavenStyle := true

pomIncludeRepository := { _ => false }

pomExtra := {
  <url>http://github.com/pathikrit/dijon</url>
  <scm>
    <url>git@github.com:pathikrit/dijon.git</url>
    <connection>scm:git:git@github.com:pathikrit/dijon.git</connection>
  </scm>
  <developers>
    <developer>
      <id>pathikrit</id>
      <name>Pathikrit Bhowmick</name>
      <url>http://github.com/pathikrit</url>
    </developer>
  </developers>
}
