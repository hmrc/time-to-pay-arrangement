resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.7")
addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "2.1.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "3.5.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "2.2.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-artifactory" % "2.0.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.3.7")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.1")