package org.rogach.avalanche
package parallel

import BuildImports._

class ParallelRunTest extends UsefulTest {
  Avalanche.opts = new Opts(Seq("-P8"))
  test ("no tasks, instant exit") {
    new Run(Graph()) start;
  }
  test ("Single task") {
    val (_,_) = captureOutput {
      var c = false
      val a = new Task("a", once, nodeps, _ => c = true)
      new Run(Graph(Map(a() -> Set()))) start;
      c ==== true
    }
  }
  test ("Two tasks") {
    val (_,_) = captureOutput {
      var c = 0
      val b = new Task("b", once, nodeps, _ => c += 1)
      val a = new Task("a", once, _ => b, _ => c += 1)
      new Run(Graph(Map(a() -> Set(b())))) start;
      c ==== 2
    }
  }
}
