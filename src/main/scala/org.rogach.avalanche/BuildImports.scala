package org.rogach.avalanche

import java.io.File

object BuildImports {
  def task(name: String, rerun: List[String] => Boolean, deps: List[String] => Seq[TaskDep], body: List[String] => Unit): Task = {
    val t = new Task(name, rerun, deps, body)
    Avalanche.tasks += t
    t
  }

  def task(name: String, inputs: List[String] => Seq[File], outputs: List[String] => Seq[File], deps: List[String] => Seq[TaskDep] = _ => Nil, body: List[String] => Unit): Task =
    task(name,
      rerun = { args =>
        val inputsModify = inputs(args).map(f => 
          if (f.exists) Some(f.lastModified) 
          else { 
            if (!Avalanche.opts.dryRun())
              throw new InputFileNotFound(f.toString, name, args)
            None
          }
        ).flatten
        val inputsModifyTime = if (inputsModify.isEmpty) System.currentTimeMillis else inputsModify.max
        
        val outputsModify = outputs(args).map(_.lastModified)
        val outputsModifyTime = if (outputsModify.isEmpty) 0 else outputsModify.min
        inputsModifyTime > outputsModifyTime
      },
      deps = deps,
      body = body
    )
    
  def onInit(fn: => Unit) = {
    Avalanche.init += (() => fn)
  }
    
  def files(names: String*)(args: List[String]) = names.map(_.format(args:_*)).map(new File(_))

  implicit def task2taskDep(t: Task) = TaskDep(t, Nil)
  implicit def taskDep2taskDepSeq(t: TaskDep) = Seq(t)
  implicit def task2taskDepSeq(t: Task) = Seq(TaskDep(t, Nil))
  implicit def taskSeq2taskDepSeq(ts: Seq[Task]) = ts.map(TaskDep(_, Nil))
}
