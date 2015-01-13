package org.rogach.avalanche

import org.rogach.scallop._

class Opts(args: Seq[String]) extends ScallopConf(args) {
  version("Avalanche, %s b%s (%3$td.%3$tm.%3$tY %3$tH:%3$tM). Built with Scala %4$s" format (
    BuildInfo.version,
    BuildInfo.buildinfoBuildnumber,
    new java.util.Date(BuildInfo.buildTime),
    BuildInfo.scalaVersion))
  banner("""Small and simple make utility clone, that uses plain Scala files for build definitions.
           |Usage:
           |  av [OPTION]... [TASK]...
           |""".stripMargin)

  val buildFile = opt[String]("build-file", descr = "use the given file instead of default av.scala")
  val noBuildCache = opt[Boolean](descr = "don't use build classes cache")
  val suppressedTasks = opt[List[String]]("suppress", short = 'S', descr = "tasks to be suppressed", default = Some(Nil))
  val tasksToRun = opt[List[String]]("tasks", descr = "tasks to run", default = Some(Nil))
  val forcedTasks = opt[List[String]]("force", descr = "force several tasks to re-build (with parameters)", argName = "tasks", default = Some(Nil))
  val allForced = opt[Boolean]("force-all", short = 'F', descr = "force all depended tasks to be rebuilded")
  val dryRun = opt[Boolean]("dry-run", short = 'D', descr = "only list the tasks in order of their execution, do not build anything")
  val listTasks = opt[Boolean]("list-tasks", short = 'L', descr = "only print list of tasks and exit")
  val splitLogs = opt[Boolean]("split-logs", short = 'W', descr = "if set, then separate directory 'logs' is created, and log file for each task is created.")
  val ignoreLock = opt[Boolean]("ignore-lock", descr = "ignore lock, that stops current build if other build process is running")
  val parallel = opt[Int]("parallel", short = 'P', descr = "controls the maximum amount of parallel tasks (threads) executing (see Task.threads). Defaults to serial execution", default = Some(1))
  private val quiet = opt[Boolean]("quiet", descr = "suppress avalanche output")
  private val silent = opt[Boolean]("silent", descr = "suppress all output, including output from scripts (stderr from scripts is still printed)")
  private val verbose = tally("verbose", descr = "print more information")
  val tasks = trailArg[List[String]]("tasks to run", descr = "tasks to run", required = false, default = Some(Nil))
  val noTimings = opt[Boolean](hidden = true)
  val profile = opt[Boolean]("profile", hidden = true, noshort = true)

  lazy val suppressedTaskDeps = suppressedTasks().map(Utils.TaskDepParser.apply)
  def isSuppressed(td: TaskDep) =
    suppressedTaskDeps.find(_._1 == td.task.name).find(_._2.filterNot(_ == td.args).isEmpty).isDefined

  def requestedTasks:List[String] =
    (tasksToRun() ::: forcedTasks() ::: tasks()).distinct

  lazy val forcedTaskDeps = forcedTasks().map(Utils.TaskDepParser.apply)
  def isForced(td: TaskDep) =
    forcedTaskDeps.find(_._1 == td.task.name).find(_._2.filterNot(_ == td.args).isEmpty).isDefined

  def isQuiet = quiet() || silent()
  def isSilent = silent()
  def isVerbose = verbose() > 0 && ! silent() && ! quiet()
  def isDebug = verbose() > 1 && ! silent() && ! quiet()
}
