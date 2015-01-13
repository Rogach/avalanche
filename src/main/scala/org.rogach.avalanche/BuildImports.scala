package org.rogach.avalanche

import avalanche._
import java.io.File

object BuildImports
extends BuildImports
with FunTasks
with BuildHelpers
with LegacyBuildImports

trait BuildImports {
  def rerunOnModifiedFiles(
      taskName: String,
      inputs: List[String] => Seq[File],
      outputs: List[String] => Seq[File])
      : List[String] => Boolean =
  { args =>
    def reportFiles(msg: String, fs: Seq[File], ln: Int) = if (Avalanche.opts.isDebug) {
      fs.foreach { f =>
        if (f.exists) {
          printf("%-6s: %2$tF %2$tT | %3$s\n", msg, f.lastModified, f)
        } else {
          printf("%-6s: %-19s | %s\n", msg, "not found", f)
        }
      }
    }
    val ln = {
      val fs = inputs(args) ++ outputs(args)
      if (fs.size > 0) fs.map(_.toString.size).max else 0
    }

    val inputFiles = inputs(args)
    val outputFiles = outputs(args)

    reportFiles("input", inputFiles, ln)
    reportFiles("output", outputFiles, ln)

    val inputsModify = inputFiles.map(f =>
      if (f.exists) Some(f.lastModified)
      else {
        if (!Avalanche.opts.dryRun())
          throw new InputFileNotFound(f.toString, taskName, args)
        Some(System.currentTimeMillis)
      }
    ).flatten
    val inputsModifyTime = if (inputsModify.isEmpty) System.currentTimeMillis else inputsModify.max

    val outputsModify = outputFiles.map(_.lastModified)
    val outputsModifyTime = if (outputsModify.isEmpty) 0 else outputsModify.min
    inputsModifyTime > outputsModifyTime
  }
}
