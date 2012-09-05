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
      error("Task failed: '%s[%s]" format (name, args.mkString(",")))
      if (!Avalanche.opts.isSilent)
        e.printStackTrace
    case TaskDepParseException(s) =>
      error("Failed to parse task dep: '%s'" format s)
    case a => 
      error("Internal exception, please file bug report!")
      a.printStackTrace
  }
}
