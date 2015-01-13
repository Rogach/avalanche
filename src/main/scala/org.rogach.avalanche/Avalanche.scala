package org.rogach.avalanche

import java.io.File
import avalanche._

object Avalanche {
  val startTime = System.currentTimeMillis
  var opts: Opts = null
  @volatile var finished = false

  val tasks = collection.mutable.ListBuffer[Task]()
  val init = collection.mutable.ListBuffer[() => Unit]()

  def main(args:Array[String]) {
    val lock = new File(".av.lock")
    try {
      opts = new Opts(args)

      if (lock.exists && !opts.ignoreLock()) {
        error("Detected other build process with pid '%s' running, exiting. Use --ignore-lock to force running." format io.Source.fromFile(lock).getLines.mkString)
        sys.exit(1)
      }
      if (!opts.ignoreLock()) {
        lock.createNewFile
        val lockOut = new java.io.PrintWriter(lock)
        lockOut.println(java.lang.management.ManagementFactory.getRuntimeMXBean.getName)
        lockOut.close
      }

      if (opts.splitLogs()) {
        // ensure directory for logs exists
        new File("logs") mkdirs
      }

      success("Starting avalanche...") // needed to eagerly initialize package object with logging logic
      sys.addShutdownHook {
        if (!opts.ignoreLock()) lock.delete
        if (!finished) error("Detected abnormal exit! (some good soul issued ^C or good ol' kill)")
        if (opts.profile()) {
          Profiler.dump()
        }
      }

      val file = opts.buildFile.get.getOrElse("av.scala")
      if (!(new File(file)).exists) throw new BuildFileNotFound(file)
      profile("Avalanche/compile build") {
        BuildCompiler.compile(file)
      }

      // print task list
      if (opts.listTasks()) {
        println("Available tasks:")
        tasks.foreach(t => println("  " + t.name))
        finished = true
        sys.exit(1)
      }

      // ensure that task names in options are parsed
      // and errors are thrown when needed
      opts.suppressedTaskDeps
      opts.forcedTaskDeps

      if (!opts.dryRun())
        init.foreach(_())

      // create a "root" task, that would trigger the build
      val rootTask = new Task(
        name = "-avalanche-root-task",
        rerun = _ => true,
        deps = _ =>
          if (opts.requestedTasks.isEmpty) List(tasks.find(_.name == "default").map(BuildImports.task2taskDep).getOrElse(throw new TaskNotFound("default")))
          else opts.requestedTasks.map(Utils.parseTaskDep),
        body = _ => ())
      val rootDep = TaskDep(task = rootTask, args = Nil)

      var tasksToRun = Graph[TaskDep]()
      // function to add task and its dependencies to the task graph, recursively
      def addDepsToGraph(t: TaskDep): Unit = {
        t.getDeps.foreach { td =>
          if (tasksToRun.map.keySet.contains(td)) {
            tasksToRun = tasksToRun.addEdge(t, td)
            // deps of td were already added
          } else {
            tasksToRun = tasksToRun.addEdge(t, td)
            addDepsToGraph(td)
          }
        }
      }
      addDepsToGraph(rootDep)

      new Run(tasksToRun) start;

      if (!opts.dryRun()) {
        printSuccessBanner
        if (!opts.noTimings())
          success("Total time: %d s, completed %s" format ((System.currentTimeMillis - startTime) / 1000, now))
      }
    } catch (ErrorHandler)
    finally {
      finished = true
    }
  }

}
