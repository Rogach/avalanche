import org.scalatest._
import java.io.File
import sbt.{IO, PathFinder, file}
import sbt.FileFilter._
import java.io._
import sys.process._

/** Test harness to run tests in src/avtest */
class AvTest(assembly: File) extends FunSuite {
  val tests = file("src/avtest").listFiles.filter(_.isDirectory).filter(!_.getName.endsWith(".result"))
  IO.delete(file("target/avtest"))
  file("target/avtest").mkdirs
  tests.foreach { testDir =>
    test("running: %s" format testDir.getName) {
      val outputDir = file("target/avtest/" + testDir.getName)
      IO.copyDirectory(testDir, outputDir)
      val java = sys.env.get("JAVA").map(_ + "/bin/java").getOrElse("java")
      val (exit, out, err) =
        runProcess(Process(Seq(java, "-jar", assembly.toString, "--no-timings"), outputDir))
      IO.write(file(outputDir.toString + ".out"), out)
      IO.write(file(outputDir.toString + ".err"), err)

      compareDirs(file("src/avtest/%s.result" format testDir.getName), outputDir)

      val expectedExit = IO.read(file("src/avtest/%s.exit" format testDir.getName)).trim.toInt
      assert(exit == expectedExit,
        "Exit code error: expected %d, got %d" format (expectedExit, exit))

      val expectedOut = IO.read(file("src/avtest/%s.out" format testDir.getName))
      compareStrings(out, expectedOut, "stdout differs")

      val expectedErr = IO.read(file("src/avtest/%s.err" format testDir.getName))
      compareStrings(err, expectedErr, "stderr differs")
    }
  }

  def runProcess(proc: ProcessBuilder): (Int, String, String) = {
    val stdout = new ByteArrayOutputStream
    val stderr = new ByteArrayOutputStream
    val stdoutWriter = new PrintWriter(stdout)
    val stderrWriter = new PrintWriter(stderr)
    val exitValue = proc.!(ProcessLogger(stdoutWriter.println, stderrWriter.println))
    stdoutWriter.close()
    stderrWriter.close()
    (exitValue, stdout.toString, stderr.toString)
  }

  def compareStrings(str1: String, str2: String, msg: String) =
    assert(
      str1 == str2,
      msg + "\n>>>>>>>>>>>>>>>>>>>>\n" +
      str1 + "====================\n" +
      str2 + "<<<<<<<<<<<<<<<<<<<<")

  /** Only compares files - not directories, because git doesn't store directories anyway */
  def compareDirs(dir1: File, dir2: File) = {
    def getFileNames(dir: File): Seq[String] =
      if (dir.exists)
        PathFinder(dir).***.filter(_.isFile).get
        .map(_.getAbsolutePath.stripPrefix(dir.getAbsolutePath))
      else Nil

    val files1 = getFileNames(dir1)
    val files2 = getFileNames(dir2)
    files1.foreach { f1 =>
      assert(files2.find(f1==).isDefined, "file '%s' is missing from output" format f1)
      compareStrings(
        IO.read(file(dir2 + f1)),
        IO.read(file(dir1 + f1)),
        "file '%s' is different in output:" format f1)
    }
    files2.foreach { f2 =>
      assert(files1.find(f2==).isDefined, "file '%s' is new in output" format f2)
    }
  }

}
