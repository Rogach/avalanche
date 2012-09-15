import AssemblyKeys._

name := "avalanche"

version := "1.6.0"

scalaVersion := "2.9.2"

scalacOptions += "-deprecation"

scalacOptions += "-unchecked"

seq(Revolver.settings: _*)

seq(assemblySettings:_*)

mainClass in assembly := Some("org.rogach.avalanche.Avalanche")

jarName in assembly <<= version ( "avalanche-%s.jar" format _ )

libraryDependencies ++= Seq(
  "org.rogach" %% "scallop" % "0.5.2",
  "org.rogach" %% "prelude" % "0.1.14",
  "org.scala-lang" % "scala-compiler" % "2.9.2",
  "commons-io" % "commons-io" % "2.1",
  "org.scalatest" %% "scalatest" % "1.7.1" % "test",
  "com.typesafe.akka" % "akka-actor" % "2.0.3"
)

resolvers ++= Seq(
  "Rogach's maven repo" at "https://github.com/Rogach/org.rogach/raw/master/",
  "sonatype" at "https://oss.sonatype.org/content/groups/public/",
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/"
)


buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, buildInfoBuildNumber)

buildInfoKeys ++= Seq[BuildInfoKey]("buildTime" -> System.currentTimeMillis)

buildInfoPackage := "org.rogach.avalanche"
