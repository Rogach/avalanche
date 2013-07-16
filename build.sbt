import AssemblyKeys._

version := "1.7.0"

name := "avalanche"

scalaVersion := "2.10.2"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-optimize",
  "-feature",
  "-Yinline-warnings",
  "-language:postfixOps",
  "-language:reflectiveCalls",
  "-language:implicitConversions"
)

seq(assemblySettings:_*)

mainClass in assembly := Some("org.rogach.avalanche.Avalanche")

jarName in assembly <<= version map (v => "avalanche-%s.jar" format v)

libraryDependencies <++= scalaVersion (sv => Seq(
  "org.rogach" %% "scallop" % "0.9.2",
  "org.scala-lang" % "scala-compiler" % sv,
  "commons-io" % "commons-io" % "2.4",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.2.0"
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
  "buildTime" -> {() => System.currentTimeMillis}
)

buildInfoPackage := "org.rogach.avalanche"
