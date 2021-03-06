package org.rogach.avalanche

import util.parsing.combinator._

object Utils {
  object TaskDepParser extends JavaTokenParsers {
    def name: Parser[String] = """[^\[\],]+""".r
    def expr: Parser[(String, Option[List[String]])] =
      name ~ opt("[" ~> repsep(name, ",") <~ "]") ^^ { case name ~ args => (name, args) }

    def apply(s: String) = parseAll(expr, s) match {
      case Success(result, _) => result
      case Failure(msg, rest) =>
        println(msg)
        println(rest)
        throw new TaskDepParseException(s)
      case Error(msg, rest) =>
        println(msg)
        println(rest)
        throw new TaskDepParseException(s)
    }
  }

  def parseTaskDep(s: String): TaskDep = {
    val r = TaskDepParser(s)
    TaskDep(
      Avalanche.tasks.find(_.name == r._1).getOrElse(throw new TaskNotFound(r._1)),
      r._2.getOrElse(Nil))
  }

}
