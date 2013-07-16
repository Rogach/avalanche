package org.rogach.avalanche

import avalanche._

object ErrorHandler extends PartialFunction[Throwable,Unit] {
  def isDefinedAt(e:Throwable) = true
  def apply(e:Throwable) = e match {
    case BuildFileNotFound(fname) =>
      error(s"Build file not found: '$fname'")
    case TaskNotFound(name) =>
      error(s"Task not found: '$name'")
    case NonZeroExitCode(Some(td), code) =>
      error(s"Task failed: $td (non-zero exit code: $code)")
    case TaskException(td, ex) =>
      error(s"Task failed with exception: $td")
      if (!Avalanche.opts.isSilent)
        ex.printStackTrace
    case TaskDepParseException(s) =>
      error(s"Failed to parse task dep: '$s'")
    case InputFileNotFound(file, taskName, args) =>
      error(s"Failed to find input file '$file' for task $taskName[${args.mkString(",")}]")
    case TaskNotCompleted(td, args) =>
      error(s"Failed to complete the task $td - after running the task, rerun is still needed.")
    case TaskSpecException(td, ex) =>
      ex match {
        case InputFileNotFound(_, _, _) =>
          apply(ex)
        case _ =>
          error(s"Exception thrown in definition of task $td:")
          if (!Avalanche.opts.isSilent)
            ex.printStackTrace
      }
    case a =>
      error("Internal exception, please file bug report!")
      a.printStackTrace
  }
}
