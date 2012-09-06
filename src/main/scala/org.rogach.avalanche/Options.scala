package org.rogach.avalanche

import org.rogach.Prelude._
import org.rogach.scallop._

class Opts(args: Seq[String]) extends ScallopConf(args) {
  version("Avalanche, %s b%s (%3$td.%3$tm.%3$tY). Built with Scala %4$s" format (
    BuildInfo.version, 
    BuildInfo.buildinfoBuildnumber, 
    new java.util.Date(BuildInfo.buildTime),
    BuildInfo.scalaVersion))
  banner("""Small and simple make utility clone, that uses plain Scala files for build definitions.
           |Usage:
           |  av [OPTION]... [TASK]...
           |""".stripMargin)
           
  val buildFile = opt[String]("build-file", descr = "use the given file instead of default av.scala")
  val supressedTasks = opt[List[String]]("supress", short = 'S', descr = "tasks to be supressed")
  val tasksToRun = opt[List[String]]("tasks", descr = "tasks to run")
  val forcedTasks = opt[List[String]]("force", descr = "force several tasks to re-build (with parameters)", argName = "tasks")
  val allForced = opt[Boolean]("force-all", short = 'F', descr = "force all depended tasks to be rebuilded")
  val dryRun = opt[Boolean]("dry-run", short = 'D', descr = "only list the tasks in order of their execution, do not build anything")
  val listTasks = opt[Boolean]("list-tasks", short = 'L', descr = "only print list of tasks and exit")
  private val quiet = opt[Boolean]("quiet", descr = "supress avalanche output")
  private val silent = opt[Boolean]("silent", descr = "supress all output, including output from scripts (stderr from scripts is still printed)")
  private val verbose = opt[Boolean]("verbose", descr = "print more information")
  val tasks = trailArg[List[String]]("tasks to run", descr = "tasks to run")

  lazy val supressedTaskDeps = supressedTasks().map(Utils.TaskDepParser.apply)
  def isSupressed(td: TaskDep) =
    supressedTaskDeps.find(_._1 == td.task.name).find(_._2.filterNot(_ == td.args).isEmpty).isDefined

  def requestedTasks:List[String] =
    (tasksToRun() ::: forcedTasks() ::: tasks()).distinct

  lazy val forcedTaskDeps = forcedTasks().map(Utils.TaskDepParser.apply)
  def isForced(td: TaskDep) =
    forcedTaskDeps.find(_._1 == td.task.name).find(_._2.filterNot(_ == td.args).isEmpty).isDefined
  
  def isQuiet = quiet() || silent()
  def isSilent = silent()
  def isVerbose = verbose() && ! silent() && ! quiet()
}
