name := "dijon"

description := "Boiler-free JSON wrangling using Scala dynamic types"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

organization := "com.github.pathikrit"

scalaVersion := "2.13.0"

crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.0")

scalacOptions ++= Seq(
  "-unchecked", "-deprecation", "-feature",
  "-language:postfixOps,implicitConversions,experimental.macros,dynamics,existentials,higherKinds"
)

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.1",
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % "0.52.2",
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "0.52.2" % Provided, // required only in compile-time
  "org.scalatest" %% "scalatest" % "3.0.8" % Test
)

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
