resolvers += Resolver.sonatypeRepo("staging")
resolvers += Resolver.bintrayIvyRepo("sbt", "sbt-plugin-releases")

val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.6.0")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.1.1")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.8.2")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.9.2")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.1")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.7")
addSbtPlugin("ch.epfl.scala" % "sbt-release-early" % "2.1.1+10-c6ef3f60")
