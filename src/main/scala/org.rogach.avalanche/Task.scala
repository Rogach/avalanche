package org.rogach.avalanche

case class Task(
    name: String,
    rerun: List[String] => Boolean,
    deps: List[String] => Seq[TaskDep],
    body: List[String] => Unit) {
  def apply(args: String*) = TaskDep(this, args.toList)
  /** Amount of threads that this task will consume if executed.
   *  Has effect only if parallel execution is enabled.
   */
  var threads = 1
}
