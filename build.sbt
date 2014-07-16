import SonatypeKeys._

name := "dijon"

version := "0.3.0"

description := "Boiler-free JSON wrangling using Scala dynamic types"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

organization := "com.github.pathikrit"

scalaVersion := "2.11.1"

crossScalaVersions := Seq("2.10.4", "2.11.1")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions,dynamics")

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.json4s" %% "json4s-native" % "3.2.9",
  "org.specs2" %% "specs2" % "2.3.12" % "test"
)

sonatypeSettings

instrumentSettings

CoverallsPlugin.coverallsSettings

autoCompilerPlugins := true

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
