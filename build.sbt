version := "2.0.1"

name := "avalanche"

scalaVersion := "2.11.4"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-optimize",
  "-feature",
  "-language:postfixOps",
  "-language:reflectiveCalls",
  "-language:implicitConversions"
)

mainClass in assembly := Some("org.rogach.avalanche.Avalanche")

assemblyJarName <<= version map (v => "avalanche-%s.jar" format v)

libraryDependencies <++= scalaVersion (sv => Seq(
  "org.rogach" %% "scallop" % "0.9.5",
  "org.scala-lang" % "scala-compiler" % sv,
  "commons-io" % "commons-io" % "2.4",
  "org.scalatest" %% "scalatest" % "2.2.2" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.3.6"
))

resolvers ++= Seq(
  "sonatype" at "https://oss.sonatype.org/content/groups/public/",
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/"
)


buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](
  name,
  version,
  scalaVersion,
  sbtVersion,
  buildInfoBuildNumber,
  BuildInfoKey.action("buildTime") {
    System.currentTimeMillis
  }
)

buildInfoPackage := "org.rogach.avalanche"
