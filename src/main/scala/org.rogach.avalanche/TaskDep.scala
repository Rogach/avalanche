package org.rogach.avalanche

import avalanche._

case class TaskDep(task: Task, args: List[String]) {
  def getDeps = task.deps(args)
  def getReRun = task.rerun(args)
  def run = {
    val needReRun = getReRun
    if (Avalanche.opts.isVerbose) {
      verbose("task: %s[%s]" format (task.name, args.mkString(",")) stripSuffix "[]")
    } else {
      if (needReRun)
      success(("task: %s[%s]" format (task.name, args.mkString(",")) stripSuffix "[]"))
    }
    if ((needReRun || Avalanche.opts.isForced(this) || Avalanche.opts.allForced()) && !Avalanche.opts.isSupressed(this)) {
      if (!Avalanche.opts.dryRun()) {
        val startTime = System.currentTimeMillis
        try {
          task.body(args)
        } catch { case e =>
          error("Exception from task (%s)" format e.getMessage)
          throw new TaskFailed(task.name, args, e)
        }

        // check that the task successfully ended
        if (getReRun) throw new TaskNotCompleted(task.name, args)

        val endTime = System.currentTimeMillis
        success("Total time: %d s, completed on " + Avalanche.TIME_FORMAT format ((endTime - startTime)/1000, new java.util.Date))        }
    } else {
      // everything's fine, we can rest
      verbose("Not rebuilding")
    }
  }
  override def equals(that: Any) = that match {
    case TaskDep(`task`, `args`) => true
    case _ => false
  }
  override def hashCode = 42 * task.hashCode + args.foldLeft(1)((c, s) => c * 42 + s.hashCode)
}

