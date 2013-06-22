import sbt._
import Keys._
import sbtassembly.Plugin._
import sbtassembly.Plugin.AssemblyKeys._
import org.scalatest

object build extends Build {
  val avTest = TaskKey[Unit]("av-test") <<= (assembly) map { (assemblyFile) =>
    scalatest.run(new AvTest(assemblyFile))
  }

  val avTestCopy =
    TaskKey[Unit]("av-test-copy") <<= (sourceDirectory, target) map { (source, target) =>
      (target / "avtest").listFiles.foreach { f =>
        if (f.isDirectory) {
          val testResult = source / "avtest" / (f.getName + ".result")
          IO.delete(testResult)
          IO.copyDirectory(f, testResult)
        } else
          if (f.isFile && List(".out", ".err", ".exit").exists(f.getName.endsWith))
            IO.copyFile(f, source / "avtest" / f.getName)
      }
    }

  lazy val root = Project("main", file(".")) settings (avTest, avTestCopy)
}
