package org.rogach.avalanche
package parallel

import BuildImports._

class ParallelRunTest extends UsefulTest {
  Avalanche.opts = new Opts(Seq("-P8"))
  test ("no tasks, instant exit") {
    new Run(Graph(Nil, Nil)) runParallel;
  }
  test ("Single task") {
    val (out, err) = captureOutput {
      var c = false
      val a = new Task("a", once, nodeps, _ => c = true)
      new Run(Graph(List(a), Nil)) runParallel;
      c ==== true
    }
  }
  test ("Two tasks") {
    val (out, err) = captureOutput {
      var c = 0
      val b = new Task("b", once, nodeps, _ => c += 1)
      val a = new Task("a", once, _ => b, _ => c += 1)
      new Run(Graph(List(a,b), List(a() -> b()))) runParallel;
      c ==== 2
    }
    
  }
}
