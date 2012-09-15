package org.rogach.avalanche

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import java.io.ByteArrayOutputStream

class UsefulTest extends FunSuite with ShouldMatchers {
  implicit def to_====[A](a: A) = new {
    def ====[B](b: B) = a should equal (b)
  }

  /** Captures all output from the *fn* block into two strings - (stdout, stderr). */
  def captureOutput(fn: => Unit):(String,String) = {
    val normalOut = Console.out
    val normalErr = Console.err
    val streamOut = new ByteArrayOutputStream()
    val streamErr = new ByteArrayOutputStream()
    Console.setOut(streamOut)
    Console.setErr(streamErr)
    fn
    Console.setOut(normalOut)
    Console.setErr(normalErr)
    (streamOut.toString, streamErr.toString)
  }

}
