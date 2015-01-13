package org.rogach.avalanche

import akka.actor.{Actor, Props, ActorSystem, PoisonPill}
import avalanche._

class Run(tasks: Graph[TaskDep]) {

  def start = {
    val system = ActorSystem("AvalancheParallelRunners")
    val master = system.actorOf(Props(new run.Master(tasks)))
    master ! run.Next
    system.awaitTermination
  }

}

package run {
  // actor messages
  case object Start
  case object Next // try to run next task
  case class StartTask(td: TaskDep)
  case class TaskSuccess(td: TaskDep)
  case class TaskFailed(td: TaskDep, th: Throwable)

  sealed abstract class TaskState
  case object Pending extends TaskState
  case object Started extends TaskState
  case object Completed extends TaskState
  case object Failed extends TaskState
  case object Cached extends TaskState

  class Master(tasks: Graph[TaskDep]) extends Actor {
    lazy val roots: Set[TaskDep] = tasks.roots
    val status = collection.mutable.Map[TaskDep, TaskState]((tasks.nodes.map(_ -> Pending)).toSeq:_*)
    val exceptions = collection.mutable.ListBuffer[(TaskDep, Throwable)]()
    var threads = Avalanche.opts.parallel()
    val taskQueue = collection.mutable.ArrayBuffer[TaskDep](tasks.depthFirstSearch(roots):_*)

    def receive = {
      case Next =>
        profile("Run/select next task") {
          val pendingTasks = taskQueue.view.filter {
            t => status(t) == Pending &&
            (tasks(t).forall(d => status(d) == Completed || status(d) == Cached))
          }
          if (Avalanche.opts.parallel() == threads) {
            pendingTasks.headOption
          } else if (threads > Avalanche.opts.parallel() / 2) {
            pendingTasks.find(t => t.task.threads <= threads)
          } else {
            pendingTasks.toList
            .sortBy(_.task.threads).reverse
            .dropWhile(t => t.task.threads > threads)
            .headOption
          }
        }.map { t =>
          if (t.task.name == "-avalanche-root-task" || t.task.body == BuildImports.NoBody) {
            status(t) =
              if (tasks(t).exists(d => status(d) == Completed)) Completed
              else Cached
            taskQueue -= t
          } else {
            try {
              verbose(s"Trying task '$t', on ${now}")
              if (!Avalanche.opts.isSuppressed(t)) {
                val needsReRun =
                  t.getReRun || Avalanche.opts.isForced(t) || Avalanche.opts.allForced()
                if (Avalanche.opts.dryRun()) {
                  if (needsReRun || tasks(t).exists(d => status(d) == Completed)) {
                    success(s"task '$t'")
                    status(t) = Completed
                    taskQueue -= t
                  } else {
                    verbose("Not rebuilding")
                    status(t) = Cached
                    taskQueue -= t
                  }
                } else {
                  if (needsReRun) {
                    context.actorOf(Props[Runner]) ! StartTask(t)
                    threads -= t.task.threads
                    status(t) = Started
                    taskQueue -= t
                  } else {
                    verbose("Not rebuilding")
                    status(t) = Cached
                    taskQueue -= t
                  }
                }
              } else {
                verbose("Task suppressed")
                status(t) = Cached
                taskQueue -= t
              }
            } catch { case e:Throwable =>
              status(t) = Failed
              taskQueue -= t
              exceptions += (t -> e)
              ErrorHandler.apply(e)
            }
          }

          // there may be more free threads left, try starting new task
          self ! Next
        } getOrElse {
          // check that there are no tasks executing
          if (threads == Avalanche.opts.parallel()) {
            context.system.shutdown
            exceptions.headOption.foreach { _ =>
              error("There were errors during parallel execution of tasks:")
              exceptions.map(_._1).foreach { td =>
                error(s"  $td")
              }

              Avalanche.finished = true
              error("BUILD FAILED")
              if (!Avalanche.opts.noTimings())
                error("Total time: %d s, completed %s" format (
                  (System.currentTimeMillis - Avalanche.startTime) / 1000,
                  now)
                )
              sys.exit(1)
            }
          }
        }
      case TaskSuccess(t) =>
        status(t) = Completed
        taskQueue -= t
        threads += t.task.threads
        self ! Next
      case TaskFailed(t, ex) =>
        status(t) = Failed
        taskQueue -= t
        threads += t.task.threads
        exceptions += (t -> ex)
        ErrorHandler.apply(ex)
        self ! Next
    }
  }

  class Runner extends Actor {
    def receive = {
      case StartTask(td) if td.task.name == "-avalanche-root-task" =>
        sender ! TaskSuccess(td)
        self ! PoisonPill
      case StartTask(td) =>
        val res = try {
          td.run
          TaskSuccess(td)
        } catch { case e:Throwable => TaskFailed(td, e) }
        sender ! res
        self ! PoisonPill
    }
  }
}
