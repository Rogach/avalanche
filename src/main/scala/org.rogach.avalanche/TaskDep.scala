package org.rogach.avalanche

import avalanche._
import java.util.Date

case class TaskDep(task: Task, args: List[String]) {
  def getDeps = try { task.deps(args) } catch { case e => throw new TaskSpecException(this, e) }
  def getReRun = try { task.rerun(args) } catch { case e => throw new TaskSpecException(this, e) }
  def run = {
    val needReRun = getReRun
    verbose("Started task '%s', on %s" format (this, now))
    if (needReRun && task.body != BuildImports.NoBody)
      success("%stask '%s', on %s" format ((if (Avalanche.opts.dryRun()) "" else "Started "), this, now))
    if ((needReRun || Avalanche.opts.isForced(this) || Avalanche.opts.allForced()) && !Avalanche.opts.isSupressed(this)) {
      if (!Avalanche.opts.dryRun()) {
        val startTime = System.currentTimeMillis
        try {
          if (Avalanche.opts.splitLogs()) {
            withLog(this.toString) {
              task.body(args)
            }
          } else {
            task.body(args)
          }
        } catch { case e =>
          error("Exception from task (%s)" format e.getMessage)
          throw new TaskFailed(task.name, args, e)
        }

        // check that the task successfully ended
        if (getReRun) throw new TaskNotCompleted(task.name, args)

        val endTime = System.currentTimeMillis
        if (task.body != BuildImports.NoBody) success("Ended task '%s', total time: %d s, completed on %s" format (this, (endTime - startTime)/1000, now))
      }
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
  override def toString = "%s[%s]" format (task.name, args.mkString(",")) stripSuffix "[]"
}

