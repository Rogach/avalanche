package org.rogach.avalanche

object BuildImports {
  def task(name: String, rerun: List[String] => Boolean, deps: List[String] => Seq[TaskDep], body: List[String] => Unit): Task = {
    val t = new Task(name, rerun, deps, body)
    Avalanche.tasks += t
    t
  }
  implicit def task2taskDep(t: Task) = TaskDep(t, Nil)
  implicit def taskDep2taskDepSeq(t: TaskDep) = Seq(t)
  implicit def task2taskDepSeq(t: Task) = Seq(TaskDep(t, Nil))
  implicit def taskSeq2taskDepSeq(ts: Seq[Task]) = ts.map(TaskDep(_, Nil))
}
