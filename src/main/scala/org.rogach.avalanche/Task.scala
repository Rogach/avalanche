package org.rogach.avalanche

case class Task(name: String, rerun: List[String] => Boolean, deps: List[String] => Seq[TaskDep], body: List[String] => Unit) {
  def apply(args: String*) = TaskDep(this, args.toList)
}
