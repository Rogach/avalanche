package org.rogach.avalanche

import avalanche._

case class BuildFileNotFound(file:String) extends Exception
case class TaskNotFound(name: String) extends Exception
case class TaskFailed(name: String, args: List[String], e: Throwable) extends Exception
case class TaskDepParseException(s: String) extends Exception
case class InputFileNotFound(name: String, task: String, args: List[String]) extends Exception
case class TaskNotCompleted(task: String, args: List[String]) extends Exception
case class VeryThreadyTask(td: TaskDep) extends Exception
case class TaskSpecException(td: TaskDep, e: Throwable) extends Exception
