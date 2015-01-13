package org.rogach.avalanche

import java.io.{PrintStream, File}

package object avalanche {
  val ERROR = "error"
  val WARN = "warn"
  val SUCCESS = "success"
  val VERBOSE = "verbose"
  val INFO = "info"

  lazy val logOutput = new util.DynamicVariable[PrintStream](System.out)
  def withLog[A](s: String)(fn: => A): A = {
    val fout = new PrintStream("logs/%s.log" format s)
    try {
      logOutput.withValue(fout)(fn)
    } finally fout.close
  }

  def log(mess:String, level:String):Unit = {
    if (Avalanche.opts == null || !Avalanche.opts.isQuiet) {
      val prefix =
        if (System.console() == null || logOutput.value != System.out) "[avalanche] "
        else {
         "[\u001b[%smavalanche\u001b[0m] " format (
            level match {
              case ERROR => "31"
              case SUCCESS => "32"
              case WARN => "33"
              case _ => "0"
            }
          )
        }
      println(prefix + mess)
    }
  }
  def log(mess:String):Unit = log(mess, INFO)
  def error(mess:String) = log(mess, ERROR)
  def warn(mess:String) = log(mess, WARN)
  def success(mess:String) = log(mess, SUCCESS)
  def verbose(mess:String) = {
    if (Avalanche.opts == null || Avalanche.opts.isVerbose)
      log(mess, VERBOSE)
  }

  val TIME_FORMAT = "%1$tb %1$te, %1$tT"
  def now = TIME_FORMAT format (new java.util.Date)

  val success_banner = """
  ██████  ██    ██   █████    █████   ██████  ██████  ██████
  ██      ██    ██  ██   ██  ██   ██  ██      ██      ██
  ██      ██    ██  ██       ██       ██      ██      ██
  ██████  ██    ██  ██       ██       ██████  ██████  ██████
      ██  ██    ██  ██       ██       ██          ██      ██
      ██  ██    ██  ██   ██  ██   ██  ██          ██      ██
  ██████   ██████    █████    █████   ██████  ██████  ██████
"""

  def printSuccessBanner = {
    if (System.console() != null) {
      print("\u001b[32m")
      print(success_banner)
      println("\u001b[0m")
    } else {
      print(success_banner)
      println("")
    }
  }

  def timed[A](name: String)(fn: => A): A = {
    val t = System.currentTimeMillis
    try {
      fn
    } finally verbose(s"Elapsed ($name): ${System.currentTimeMillis - t} ms")
  }

}
