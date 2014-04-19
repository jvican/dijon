resolvers ++= Seq(
  Classpaths.sbtPluginReleases
)

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.2.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.1")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.98.2")

addSbtPlugin("com.sksamuel.scoverage" %% "sbt-coveralls" % "0.0.5")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.5")
