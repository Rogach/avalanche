onInit {
  println("init")
}
createDirs("target/a", "target/b")
val default = task("default",
  rerun = once,
  deps = _ => List(second("a"), third),
  body = a => println("Default task, with params: %s" format a.mkString(", ")))
val second = task("second",
  rerun = once,
  deps = _ => Nil,
  body = a => println("Second task, with params: %s" format a.mkString(", ")))
val third = task("third",
  rerun = once,
  deps = _ => second("a"),
  body = a => println("Third task, with params: %s" format a.mkString(", ")))
