import SonatypeKeys._

sonatypeSettings

name := "dijon"

version := "0.2.0"

organization := "com.github.pathikrit"

description := "Boiler-free JSON wrangling using Scala dynamic types"

scalaVersion := "2.10.3"

scalacOptions ++= Seq(
  "-unchecked", "-deprecation", "-feature",
  "-language:postfixOps,implicitConversions,experimental.macros,dynamics,existentials,higherKinds"
)

ScoverageSbtPlugin.instrumentSettings

CoverallsPlugin.coverallsSettings

CoverallsPlugin.CoverallsKeys.coverallsToken := Some("m1ICpWHwSZMMvgqeKbUaRE6RFre1p3zws")

libraryDependencies ++= Seq("org.specs2" %% "specs2" % "2.3.10" % "test")

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _)

pomExtra := {
  <url>http://github.com/pathikrit/dijon</url>
  <licenses>
    <license>
      <name>The MIT License</name>
      <url>http://www.opensource.org/licenses/MIT</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
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

