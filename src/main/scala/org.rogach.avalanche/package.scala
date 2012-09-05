package org.rogach.avalanche

package object avalanche {
  val ERROR = "error"
  val WARN = "warn"
  val SUCCESS = "success"
  val VERBOSE = "verbose"
  val INFO = "info"
  
  def log(mess:String, level:String):Unit = {
    if (!Avalanche.opts.isQuiet) {
      // print prefix
      print(
        "[\033[%smavalanche\033[0m] " format (
          level match {
            case ERROR => "31"
            case SUCCESS => "32"
            case WARN => "33"
            case _ => "0"
          }
        )
      )
      println(mess)
    }
  }
  def log(mess:String):Unit = log(mess, INFO)
  def error(mess:String) = log(mess, ERROR)
  def warn(mess:String) = log(mess, WARN)
  def success(mess:String) = log(mess, SUCCESS)
  def verbose(mess:String) = {
    if (Avalanche.opts.isVerbose)
      log(mess, VERBOSE)
  }
  
}
