package org.rogach.avalanche

import java.io.File
import avalanche._
import org.rogach.Prelude._

object Avalanche {
  val TIME_FORMAT = "%2$tb %2$te, %2$tT"
  var opts: Opts = null

  val tasks = collection.mutable.ListBuffer[Task]()
  val init = collection.mutable.ListBuffer[() => Unit]()
  
  def main(args:Array[String]) {
    val startTime = System.currentTimeMillis
    val lock = new File(".av.lock")
    try {
      opts = new Opts(args)

      if (lock.exists && !opts.ignoreLock()) {
        error("Detected other build process with pid '%s' running, exiting. Use --ignore-lock to force running." format io.Source.fromFile(lock).getLines.mkString)
        sys.exit(1)
      }
      lock.createNewFile
      lock.printHere(_.println(java.lang.management.ManagementFactory.getRuntimeMXBean.getName))

      success("Starting avalanche...") // needed to eagerly initialize package object with logging logic
      sys.addShutdownHook {
        error("Detected ^C signal, ending process...")
      }

      val file = opts.buildFile.get.getOrElse("av.scala")
      if (!(new File(file)).exists) throw new BuildFileNotFound(file)
      BuildCompiler.compile(file)

      // print task list
      if (opts.listTasks()) {
        println("Available tasks:")
        tasks.foreach(t => println("  " + t.name))
        sys.exit(1)
      }
      
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

      var tasksToRun = new Graph[TaskDep](Nil, Nil)
      // function to add task and its dependencies to the task graph, recursively
      def addDepsToGraph(t: TaskDep): Unit = {
        t.getDeps.foreach { td =>
          if (tasksToRun.nodes.contains(td)) {
            tasksToRun += (td -> t)
            // deps of td were already added
          } else {
            tasksToRun += (td)
            tasksToRun += (td -> t)
            addDepsToGraph(td)
          }
        }
      }
      tasksToRun += (rootDep)
      addDepsToGraph(rootDep)
      
      tasksToRun.topologicalSort.dropRight(1).foreach(_.run)

      success("Build done.")
      success("Total time: %d s, completed " + TIME_FORMAT format ((System.currentTimeMillis - startTime) / 1000, new java.util.Date))
    } catch (ErrorHandler)
    finally {
      lock.delete
    }
  }

}
