package org.rogach.avalanche

import java.io.File
import sys.process._

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
            Some(System.currentTimeMillis)
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
    
  def files(names: String*)(args: List[String]) = names.map(_.format(args:_*)).map(new File(_))

  def onInit(fn: => Unit) = {
    Avalanche.init += (() => fn)
  }
  def createDirs(dirs: String*) =
    onInit {
      dirs.map(new File(_)).foreach(_.mkdirs)
    }


  def f(s: String): File = new File(s)

  def exec(f: String, env: Map[String,String]) = {
    val exitCode = Process(f, None, env.toSeq:_*) !;
    if (exitCode != 0) sys.error("Non-zero exit code from script: '%s'" format f)
  }  
  
  implicit def task2taskDep(t: Task) = TaskDep(t, Nil)
  implicit def taskDep2taskDepSeq(t: TaskDep) = Seq(t)
  implicit def task2taskDepSeq(t: Task) = Seq(TaskDep(t, Nil))
  implicit def taskSeq2taskDepSeq(ts: Seq[Task]) = ts.map(TaskDep(_, Nil))
}
