package org.rogach.avalanche

import java.io.File
import sys.process._

object BuildImports extends FunTasks {
  def task(name: String, rerun: List[String] => Boolean, deps: List[String] => Seq[TaskDep], body: List[String] => Unit): Task = {
    val t = new Task(name, rerun, deps, body)
    Avalanche.tasks += t
    t
  }

  def rerunOnModifiedFiles(
      taskName: String,
      inputs: List[String] => Seq[File],
      outputs: List[String] => Seq[File])
      : List[String] => Boolean =
  { args =>
    def reportFiles(msg: String, fs: Seq[File], ln: Int) = if (Avalanche.opts.isDebug) {
      fs.foreach { f =>
        if (f.exists) {
          printf("%-6s: %2$tF %2$tT | %3$s\n", msg, f.lastModified, f)
        } else {
          printf("%-6s: %-19s | %s\n", msg, "not found", f)
        }
      }
    }
    val ln = {
      val fs = inputs(args) ++ outputs(args)
      if (fs.size > 0) fs.map(_.toString.size).max else 0
    }
    reportFiles("input", inputs(args), ln)
    reportFiles("output", outputs(args), ln)

    val inputsModify = inputs(args).map(f =>
      if (f.exists) Some(f.lastModified)
      else {
        if (!Avalanche.opts.dryRun())
          throw new InputFileNotFound(f.toString, taskName, args)
        Some(System.currentTimeMillis)
      }
    ).flatten
    val inputsModifyTime = if (inputsModify.isEmpty) System.currentTimeMillis else inputsModify.max

    val outputsModify = outputs(args).map(_.lastModified)
    val outputsModifyTime = if (outputsModify.isEmpty) 0 else outputsModify.min
    inputsModifyTime > outputsModifyTime
  }

  def task(name: String, inputs: List[String] => Seq[File], outputs: List[String] => Seq[File], deps: List[String] => Seq[TaskDep] = _ => Nil, body: List[String] => Unit): Task =
    task(name,
      rerun = rerunOnModifiedFiles(name, inputs, outputs),
      deps = deps,
      body = body
    )

  def task0(name: String, inputs: => Seq[File], outputs: => Seq[File], deps: => Seq[TaskDep], body: => Unit) = {
    task(name, _ => inputs, _ => outputs, _ => deps, _ => body)
  }
  def task1(
      name: String,
      inputs: String => Seq[File],
      outputs: String => Seq[File],
      deps: String => Seq[TaskDep],
      body: String => Unit) = {
    task(name, l => inputs(l.head), l => outputs(l.head), l => deps(l.head), l => body(l.head))
  }

  val NoBody: List[String] => Unit = _ => ()
  def aggregate(name: String, deps: => Seq[TaskDep]) = task(name, once, _ => deps, NoBody)

  def files(names: String*)(args: List[String]) = names.map(_.format(args:_*)).map(new File(_))
  def glob(names: String*)(args: List[String]) =
    names.map(n => Seq("bash","-c","ls -1 %s" format (n.format(args:_*))).lineStream_!(ProcessLogger(s =>())).toList.headOption.getOrElse(n)).map(new File(_))
  def globs(names: String*)(args: List[String]) =
    names.flatMap(n => Seq("bash","-c","ls -1 %s" format (n.format(args:_*))).lineStream_!(ProcessLogger(s =>())).toList).map(new File(_))

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

  /** helper, that is used to run task only once in a build, for each set of arguments. Useful in testing.
   *  Usage:
   *  {{{
   *  task("sometask", rerun = once, ...)
   *  }}}
   */
  def once = new (List[String] => Boolean) {
    val seen = collection.mutable.HashSet[List[String]]()
    def apply(l: List[String]) =
      if (seen(l)) false
      else {
        seen += l
        true
      }
  }

  def nodeps = (a:List[String]) => Nil

  implicit def task2taskDep(t: Task) = TaskDep(t, Nil)
  implicit def taskDep2taskDepSeq(t: TaskDep) = Seq(t)
  implicit def task2taskDepSeq(t: Task) = Seq(TaskDep(t, Nil))
  implicit def taskSeq2taskDepSeq(ts: Seq[Task]) = ts.map(TaskDep(_, Nil))

  def log(msg: String) = avalanche.logOutput.value.println(msg)

}
