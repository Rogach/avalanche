package org.rogach.avalanche

import avalanche._

object ErrorHandler extends PartialFunction[Throwable,Unit] {
  def isDefinedAt(e:Throwable) = true
  def printError(msg: String) = {
    error(msg)
    if (Avalanche.opts == null || Avalanche.opts.parallel() == 1 ) {
      // in parallel execution, those messages are printed in Run.parallel
      error("BUILD FAILED")
      if (!Avalanche.opts.noTimings())
        error("Total time: %d s, completed %s" format ((System.currentTimeMillis - Avalanche.startTime) / 1000, now))
    }
  }
  def apply(e:Throwable) = e match {
    case BuildFileNotFound(fname) =>
      printError("Build file not found: %s" format fname)
    case TaskNotFound(name) =>
      printError("Task not found: '%s'" format name)
    case NonZeroExitCode(Some(TaskDep(task, args)), code) =>
      printError("Task failed: %s[%s] (non-zero exit code: %d)" format
        (task.name, args.mkString(","), code))
    case TaskFailed(name, args, e) =>
      printError("Task failed: %s[%s]" format (name, args.mkString(",")))
      if (!Avalanche.opts.isSilent)
        e.printStackTrace
    case TaskDepParseException(s) =>
      printError("Failed to parse task dep: '%s'" format s)
    case InputFileNotFound(fn, task, args) =>
      printError("Failed to find input file '%s' for task %s[%s]" format (fn, task, args.mkString(",")))
    case TaskNotCompleted(task, args) =>
      printError("Failed to complete the task '%s[%s]' - after running the task, rerun is still needed." format (task, args.mkString(", ")))
    case VeryThreadyTask(td) =>
      printError("Task '%s' requires too much threads: required = %d, max = %d." format (td, td.task.threads, Avalanche.opts.parallel()))
    case TaskSpecException(td, ex) =>
      ex match {
        case InputFileNotFound(_, _, _) =>
          apply(ex)
        case _ =>
          printError("Exception thrown in definition of task '%s':" format td)
          ex.printStackTrace
      }
    case a =>
      printError("Internal exception, please file bug report!")
      println(a)
      a.printStackTrace
  }
}
