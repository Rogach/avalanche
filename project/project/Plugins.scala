import sbt._
object Plugins extends Build {
  override lazy val projects = Seq(root)
  lazy val root = Project("plugins", file(".")).dependsOn(buildinfoPlugin)
  lazy val buildinfoPlugin = uri("git://github.com/sbt/sbt-buildinfo.git#7b0a05e10fbbc654ee653e3d1f3c6645b454c4b5")
}
