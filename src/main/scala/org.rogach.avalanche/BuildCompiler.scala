package org.rogach.avalanche

import avalanche._
import tools.nsc.{Global, Settings}
import tools.nsc.io.VirtualDirectory
import tools.nsc.reporters.StoreReporter
import tools.nsc.util._
import reflect.internal.util.AbstractFileClassLoader
import reflect.internal.util.BatchSourceFile
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File

object BuildCompiler {
  def compile(file: String) = {
    // for caching, I assume that there are no packages in build file,
    // thus there are no subdirectories in cache dir
    val cacheDir = new File(".av")
    val cachedFilesModTime: Long = {
      // iterate all files in cache dir
      if (cacheDir.exists && cacheDir.list.nonEmpty) {
        cacheDir.listFiles.map(_.lastModified).min
      } else 0L
    }
    val buildFilesModTime: Long = {
      // build file & jar file
      val jarFile = new File(
        this.getClass.getResource("/"+this.getClass.getName.replace(".","/")+".class")
        .toString.stripPrefix("jar:file:").takeWhile('!'!=)
      )
      math.max(
        (new File(file)).lastModified,
        jarFile.lastModified
      )
    }
    val classesDir = if (!Avalanche.opts.noBuildCache() && cachedFilesModTime >= buildFilesModTime) {
      // load cached build file
      success("Using cached build classes.")
      loadVirtualDir(cacheDir)
    } else {
      // re-compile build file
      val startTime = System.currentTimeMillis
      try {
        val c = new Compiler
        val classesDir = c.compile(
          // putting everything on one line allows me to get proper line numbers
          // in case of runtime errors
          "import org.rogach.avalanche.BuildImports._; class Build { %s\n}"
          format io.Source.fromFile(file).getLines.mkString("\n")
        )

        if (!Avalanche.opts.noBuildCache()) {
          saveVirtualDir(classesDir, cacheDir)
        }

        val endTime = System.currentTimeMillis
        if (Avalanche.opts.noTimings())
          success("Compiled build file")
        else
          success("Compiled build file, time elapsed: %d s" format (endTime - startTime)/1000)
        classesDir
      } catch { case e: Throwable =>
        error(s"Failed to compile build file '$file'")
        e.printStackTrace
        Avalanche.finished = true
        sys.exit(1)
      }
    }
    try {
      loadClassesFromDir(classesDir).head.newInstance
    } catch { case e: Throwable =>
      error(s"Failed to run build file '$file'")
      e.printStackTrace
      Avalanche.finished = true
      sys.exit(1)
    }
  }

  def saveVirtualDir(v: VirtualDirectory, dir: File) {
    FileUtils.deleteQuietly(dir)
    dir.mkdirs
    v.foreach { f =>
      FileUtils.writeByteArrayToFile(new File(dir + "/" + f.name), f.toByteArray)
    }
  }

  def loadVirtualDir(dir: File): VirtualDirectory = {
    val v = new VirtualDirectory("(memory)", None)
    dir.listFiles.foreach { f =>
      val vf = v.fileNamed(f.getName)
      val vfo = vf.output
      val ifs = FileUtils.openInputStream(f)
      IOUtils.copy(ifs, vfo)
      ifs.close
      vfo.close
    }
    v
  }

  def loadClassesFromDir(directory: VirtualDirectory): Iterable[Class[_]] = {
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

// from habrahabr.ru

/*!# Compiler

   This class is a wrapper over Scala Compiler API
   which has simple interface just accepting the source code string.

   Compiles the source code assuming that it is a .scala source file content.
   It used a classpath of the environment that called the `Compiler` class.
  */

class Compiler {

   def compile(source: String): VirtualDirectory = {

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

     if (reporter.hasErrors) {
       throw new CompilationFailedException(
         source,
         reporter.infos.map(info =>
           CompileError(
             info.pos.line,
             info.pos.column,
             info.pos.lineContent,
             info.msg
           )
         ).toList.sortBy(_.line)
       )
     }

     directory
   }
}

/*!### Compilation exception

   Compilation exception is defined this way.
   It contains program was compiling and error positions with messages
   of what went wrong during compilation.
  */
class CompilationFailedException(
  val programme: String,
  val messages: Iterable[CompileError]
) extends Exception(
  "\n" + messages.map(m =>
    "line %d: %s \n    %s\n    %s" format (
      m.line,
      m.message,
      if (m.line != 1) m.lineContent else m.lineContent.drop(58),
      " " * (if (m.line != 1) m.column - 1 else m.column - 59) + "^"
    )
  ).mkString("\n")
)

case class CompileError(line: Int, column: Int, lineContent: String, message: String)
