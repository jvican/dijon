name := "yasjl"

version := "0.0.1"

scalaVersion := "2.10.3"

scalacOptions ++= Seq(
  "-unchecked", "-deprecation", "-feature",
  "-language:postfixOps,implicitConversions,experimental.macros,dynamics,existentials"
)

libraryDependencies ++= Seq(
  "org.specs2" % "specs2_2.10" % "2.1.1" % "test"
)
