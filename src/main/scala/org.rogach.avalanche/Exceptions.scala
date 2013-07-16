package org.rogach.avalanche

import avalanche._

case class BuildFileNotFound(file:String) extends Exception
case class TaskNotFound(name: String) extends Exception
case class TaskException(td: TaskDep, e: Throwable) extends Exception
case class NonZeroExitCode(td: Option[TaskDep], code: Int) extends Exception
case class TaskDepParseException(s: String) extends Exception
case class InputFileNotFound(name: String, task: String, args: List[String]) extends Exception
case class TaskNotCompleted(task: String, args: List[String]) extends Exception
case class TaskSpecException(td: TaskDep, e: Throwable) extends Exception
