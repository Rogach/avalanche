package org.rogach.avalanche

import avalanche._
import java.io.File

trait LegacyBuildImports { this: BuildImports =>
  def task(name: String, rerun: List[String] => Boolean, deps: List[String] => Seq[TaskDep], body: List[String] => Unit): Task = {
    val t = new Task(name, rerun, deps, body)
    Avalanche.tasks += t
    t
  }

  def task(name: String, inputs: List[String] => Seq[File], outputs: List[String] => Seq[File], deps: List[String] => Seq[TaskDep] = _ => Nil, body: List[String] => Unit): Task =
    task(name,
      rerun = rerunOnModifiedFiles(name, inputs, outputs),
      deps = deps,
      body = body
    )

  def task0(name: String, inputs: => Seq[File], outputs: => Seq[File], deps: => Seq[TaskDep], body: => Unit) = {
    task(name, _ => inputs, _ => outputs, _ => deps, _ => body)
  }
  def task1(
      name: String,
      inputs: String => Seq[File],
      outputs: String => Seq[File],
      deps: String => Seq[TaskDep],
      body: String => Unit) = {
    task(name, l => inputs(l.head), l => outputs(l.head), l => deps(l.head), l => body(l.head))
  }

  val NoBody: List[String] => Unit = _ => ()
  def aggregate(name: String, deps: => Seq[TaskDep]) = task(name, once, _ => deps, NoBody)

  /** helper, that is used to run task only once in a build, for each set of arguments. Useful in testing.
   *  Usage:
   *  {{{
   *  task("sometask", rerun = once, ...)
   *  }}}
   */
  def once = new (List[String] => Boolean) {
    val seen = collection.mutable.HashSet[List[String]]()
    def apply(l: List[String]) =
      if (seen(l)) false
      else {
        seen += l
        true
      }
  }

  def nodeps = (a:List[String]) => Nil

  implicit def task2taskDep(t: Task) = TaskDep(t, Nil)
  implicit def taskDep2taskDepSeq(t: TaskDep) = Seq(t)
  implicit def task2taskDepSeq(t: Task) = Seq(TaskDep(t, Nil))
  implicit def taskSeq2taskDepSeq(ts: Seq[Task]) = ts.map(TaskDep(_, Nil))

}
