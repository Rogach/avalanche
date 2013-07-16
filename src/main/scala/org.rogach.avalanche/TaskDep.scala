package org.rogach.avalanche

import avalanche._
import java.util.Date

case class TaskDep(task: Task, args: List[String]) {
  def getDeps =
    try { task.deps(args) } catch { case e:Throwable => throw new TaskSpecException(this, e) }
  def getReRun =
    try { task.rerun(args) } catch { case e:Throwable => throw new TaskSpecException(this, e) }
  def run = {
    verbose("Starting task '%s', on %s" format (this.toString, now))
    if (!Avalanche.opts.isSupressed(this)) {
      val needReRun = getReRun
      if (needReRun || Avalanche.opts.isForced(this) || Avalanche.opts.allForced()) {
        if (task.body != BuildImports.NoBody && Avalanche.opts.dryRun()) {
          success("task '%s'" format this.toString)
        }
        if (!Avalanche.opts.dryRun()) {
          if (task.body != BuildImports.NoBody) {
            if (Avalanche.opts.noTimings())
              success("Started task '%s'")
            else
              success("Started task '%s', on %s" format (this.toString, now))
          }
          val startTime = System.currentTimeMillis
          try {
            if (Avalanche.opts.splitLogs()) {
              withLog(this.toString) {
                task.body(args)
              }
            } else {
              task.body(args)
            }
          } catch {
            case NonZeroExitCode(None, code) => throw NonZeroExitCode(Some(this), code)
            case e: Throwable => throw new TaskException(this, e)
          }

          // check that the task successfully ended
          if (getReRun) throw new TaskNotCompleted(this)

          val endTime = System.currentTimeMillis
          if (task.body != BuildImports.NoBody)
            if (Avalanche.opts.noTimings())
              success("Ended task '%s'" format this)
            else
              success("Ended task '%s', total time: %d s, completed on %s" format
                (this, (endTime - startTime)/1000, now))
        }
      } else {
        // everything's fine, we can rest
        verbose("Not rebuilding")
      }
    } else {
      verbose("Task suppressed")
    }
  }
  override def equals(that: Any) = that match {
    case TaskDep(`task`, `args`) => true
    case _ => false
  }
  override def hashCode = 42 * task.hashCode + args.foldLeft(1)((c, s) => c * 42 + s.hashCode)
  override def toString = "%s[%s]" format (task.name, args.mkString(",")) stripSuffix "[]"
}
