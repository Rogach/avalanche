package org.rogach.avalanche

import avalanche._

object ErrorHandler extends PartialFunction[Throwable,Unit] {
  def isDefinedAt(e:Throwable) = true
  def apply(e:Throwable) = e match {
    case BuildFileNotFound(fname) =>
      error("Build file not found: %s" format fname)
    case TaskNotFound(name) =>
      error("Task not found: '%s'" format name)
    case TaskFailed(name, args, e) =>
      error("Task failed: %s[%s]" format (name, args.mkString(",")))
      if (!Avalanche.opts.isSilent)
        e.printStackTrace
    case TaskDepParseException(s) =>
      error("Failed to parse task dep: '%s'" format s)
    case InputFileNotFound(fn, task, args) =>
      error("Failed to find input file '%s' for task %s[%s]" format (fn, task, args.mkString(",")))
    case TaskNotCompleted(task, args) =>
      error("Failed to complete the task '%s[%s]' - after running the task, rerun is still needed." format (task, args))
    case VeryThreadyTask(td) =>
      error("Task '%s' requires too much threads: required = %d, max = %d." format (td, td.task.threadAmount, Avalanche.opts.parallel()))
    case TaskSpecException(td, ex) =>
      error("Exception thrown in definition of task '%s':" format td)
      ex.printStackTrace
    case a => 
      error("Internal exception, please file bug report!")
      a.printStackTrace
  }
}
