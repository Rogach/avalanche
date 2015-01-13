package org.rogach.avalanche

import avalanche._
import java.io.File
import sys.process._

trait BuildHelpers {
  def files(names: String*)(args: List[String]) = names.map(_.format(args:_*)).map(new File(_))

  private def bash_glob(g: String): List[String] =
    Seq("bash", "-c", "ls -1 " + g).lineStream_!(ProcessLogger(s =>())).toList

  def glob(names: String*): List[File] =
    names.map(n => bash_glob(n).headOption.getOrElse(n)).map(new File(_)).toList
  def globs(names: String*): List[File] =
    names.flatMap(bash_glob).map(new File(_)).toList

  def pwd = f(".").getAbsoluteFile.getParent

  def onInit(fn: => Unit) = {
    Avalanche.init += (() => fn)
  }
  def createDirs(dirs: String*) =
    onInit {
      dirs.map(new File(_)).foreach(_.mkdirs)
    }

  def f(s: String): File = new File(s)

  def exec(pb: ProcessBuilder): Unit = {
    val out = avalanche.logOutput.value
    val exitCode = pb.!(ProcessLogger(out.println, out.println))
    if (exitCode != 0) throw NonZeroExitCode(None, exitCode)
  }
  def exec(f: String): Unit = exec(f, Map[String,Any]())
  def exec(f: String, env: Map[String,Any]): Unit = exec(Process(f, None, env.mapValues(_.toString).toSeq:_*))
  def exec(cmd: Seq[String]): Unit = exec(cmd, Map[String,Any]())
  def exec(cmd: Seq[String], env: Map[String,Any]): Unit = exec(Process(cmd, None, env.mapValues(_.toString).toSeq:_*))


  def log(msg: String) = avalanche.logOutput.value.println(msg)
}
