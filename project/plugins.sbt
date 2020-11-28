resolvers += Resolver.sonatypeRepo("staging")
resolvers += Resolver.bintrayIvyRepo("sbt", "sbt-plugin-releases")

addSbtPlugin("com.dwijnand" % "sbt-dynver" % "3.1.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.8.1")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.7")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.8.1")
addSbtPlugin("ch.epfl.scala" % "sbt-release-early" % "2.1.1+10-c6ef3f60")
