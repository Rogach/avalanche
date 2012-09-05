package org.rogach.avalanche

import avalanche._

case class BuildFileNotFound(file:String) extends Exception
case class TaskNotFound(name: String) extends Exception
case class TaskFailed(name: String, args: List[String], e: Throwable) extends Exception
case class TaskDepParseException(s: String) extends Exception
