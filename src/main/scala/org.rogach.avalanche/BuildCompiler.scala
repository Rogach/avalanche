package org.rogach.avalanche

import avalanche._

object BuildCompiler {
  def compile(file: String) = {
    val startTime = System.currentTimeMillis
    try {
      val c = new Compiler
      val classes = c.compile(
        """
        |import org.rogach.avalanche.BuildImports._
        |class Build {
        |%s
        |}
        |""".stripMargin format io.Source.fromFile(file).getLines.mkString("\n")
      )
      classes.head.newInstance
      val endTime = System.currentTimeMillis
      success("Compiled build file, time elapsed: %d s" format (endTime - startTime)/1000)
    } catch { case e =>
      error("Failed to compile build file '%s'" format file)
      e.printStackTrace
      Avalanche.finished = true
      sys.exit(1)
    }
  }
}

// from habrahabr.ru

/*!# Compiler

   This class is a wrapper over Scala Compiler API
   which has simple interface just accepting the source code string.

   Compiles the source code assuming that it is a .scala source file content.
   It used a classpath of the environment that called the `Compiler` class.
  */

import tools.nsc.{Global, Settings}
import tools.nsc.io._
import tools.nsc.reporters.StoreReporter
import tools.nsc.interpreter.AbstractFileClassLoader
import tools.nsc.util._

class Compiler {

   def compile(source: String): Iterable[Class[_]] = {

     // prepare the code you want to compile
     val sources = List(new BatchSourceFile("<source>", source))

     // Setting the compiler settings
     val settings = new Settings

     /*! Take classpath from currently running scala environment. */
     settings.usejavacp.value = true

     /*! Save class files for compiled classes into a virtual directory in memory. */
     val directory = new VirtualDirectory("(memory)", None)
     settings.outputDirs.setSingleOutput(directory)

     val reporter = new StoreReporter()
     val compiler = new Global(settings, reporter)
     new compiler.Run()compileSources(sources)

     /*! After the compilation if errors occured, `CompilationFailedException`
         is being thrown with a detailed message. */
     if (reporter.hasErrors) {
       throw new CompilationFailedException(source,
         reporter.infos.map(info => CompileError(info.pos.line, info.pos.column, info.pos.lineContent, info.msg)).toList.sortBy(_.line))
     }

     /*! Each time new `AbstractFileClassLoader` is created for loading classes
       it gives an opportunity to treat same name classes loading well.
      */
     // Loading new compiled classes
     val classLoader =  new AbstractFileClassLoader(directory, this.getClass.getClassLoader())

     /*! When classes are loading inner classes are being skipped. */
     for (classFile <- directory; if (!classFile.name.contains('$'))) yield {

       /*! Each file name is being constructed from a path in the virtual directory. */
       val path = classFile.path
       val fullQualifiedName = path.substring(path.indexOf('/')+1,path.lastIndexOf('.')).replace("/",".")

       /*! Loaded classes are collecting into a returning collection with `yield`. */
       classLoader.loadClass(fullQualifiedName)
     }
   }
}

/*!### Compilation exception

   Compilation exception is defined this way.
   It contains program was compiling and error positions with messages
   of what went wrong during compilation.
  */
class CompilationFailedException(val programme: String,
                                  val messages: Iterable[CompileError])
   extends Exception("\n" + messages.map(m => "line %d: %s \n    %s\n    %s" format (m.line - 3, m.message, m.lineContent, " " * (m.column - 1) + "^")).mkString("\n"))

case class CompileError(line: Int, column: Int, lineContent: String, message: String)
