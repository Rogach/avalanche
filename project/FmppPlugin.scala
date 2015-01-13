package fmpp

import sbt._
import Keys._
import sbt.Fork

import java.io.File

object FmppPlugin extends AutoPlugin {
  override def trigger = allRequirements

  val fmpp = TaskKey[Seq[File]]("fmpp", "Generate Scala sources from FMPP Scala Template")
  val fmppArgs = SettingKey[Seq[String]]("fmpp-args", "Extra command line parameters to FMPP.")
  val fmppMain = SettingKey[String]("fmpp-main", "FMPP main class.")
  val fmppSources =  SettingKey[Seq[String]]("fmpp-sources", "Sources type to be processed.")
  val fmppVersion =  SettingKey[String]("fmpp-version", "FMPP version.")

  override lazy val projectSettings = fmppSettings

  lazy val fmppSettings = Seq[Def.Setting[_]](
    fmppArgs := Seq("--ignore-temporary-files"),
    fmppMain := "fmpp.tools.CommandLine",
    fmppSources := Seq("scala", "java"),
    fmppVersion := "0.9.15",
    libraryDependencies <+= (fmppVersion in fmpp)("net.sourceforge.fmpp" % "fmpp" % _),
    sourceDirectory in fmpp <<= (sourceDirectory in Compile),
    scalaSource in fmpp <<= (sourceManaged in Compile),

    fmpp <<= (
      fmppSources in fmpp,
      sourceDirectory in fmpp,
      sourceManaged in fmpp,
      fmppMain in fmpp,
      fmppArgs in fmpp,
      managedClasspath in Compile,
      javaHome,
      streams
    ).map(process),

    sourceGenerators in Compile <+= fmpp
  )

  private def process(
    sources: Seq[String],
    source: File,
    sourceManaged: File,
    mainClass: String,
    args: Seq[String],
    classpath: Classpath,
    javaHome: Option[File],
    streams: TaskStreams
  ) = {
    sources.flatMap(x => {
      val input = source / x
      if (input.exists) {
        val output = sourceManaged / x
        val cached = FileFunction.cached(streams.cacheDirectory / "fmpp" / x, FilesInfo.lastModified, FilesInfo.exists) {
          (in: Set[File]) => {
            IO.delete(output)
            Fork.java(
              javaHome,
              List(
                "-cp", classpath.map(_.data).mkString(File.pathSeparator), mainClass,
                "-S", input.toString, "-O", output.toString,
                "--replace-extensions=fm, " + x,
                "-M", "execute(**/*.fm), ignore(**/*)"
              ) ::: args.toList,
              streams.log
            )
            (output ** ("*." + x)).get.toSet
          }
        }
        cached((input ** "*.fm").get.toSet)
      } else Nil
    })
  }
}
