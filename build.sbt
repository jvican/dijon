name := "dijon"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.3"

scalacOptions ++= Seq(
  "-unchecked", "-deprecation", "-feature",
  "-language:postfixOps,implicitConversions,experimental.macros,dynamics,existentials,higherKinds"
)

libraryDependencies ++= Seq("org.specs2" %% "specs2" % "2.3.10" % "test")
