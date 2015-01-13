addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.12.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.3.2")

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.2"

scalacOptions ++= Seq(
  "-deprecation"
)
