resolvers += Resolver.sonatypeRepo("staging")
resolvers += Resolver.bintrayIvyRepo("sbt", "sbt-plugin-releases")

val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.15.0")
val scalaNativeVersion =
  Option(System.getenv("SCALANATIVE_VERSION")).getOrElse("0.4.4")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.1.1")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.3")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.3")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.10.0")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.11")
addSbtPlugin("ch.epfl.scala" % "sbt-release-early" % "2.1.1+10-c6ef3f60")
