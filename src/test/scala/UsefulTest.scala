package org.rogach.avalanche

import org.scalatest.{FunSuite, Matchers}
import java.io.ByteArrayOutputStream

class UsefulTest extends FunSuite with Matchers {
  implicit def to_====[A](a: A) = new {
    def ====[B](b: B) = a should equal (b)
  }

  /** Captures all output from the *fn* block into two strings - (stdout, stderr). */
  def captureOutput(fn: => Unit):(String,String) = {
    val streamOut = new ByteArrayOutputStream()
    val streamErr = new ByteArrayOutputStream()
    Console.withOut(streamOut) {
      Console.withErr(streamErr) {
        fn
      }
    }
    (streamOut.toString, streamErr.toString)
  }

}
