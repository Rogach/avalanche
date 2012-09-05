val default = task("default",
  rerun = _ => true,
  deps = _ => List(second("a"), third),
  body = a => println("Default task, with params: %s" format a.mkString(", ")))
val second = task("second",
  rerun = _ => true,
  deps = _ => Nil,
  body = a => println("Second task, with params: %s" format a.mkString(", ")))
val third = task("third",
  rerun = _ => false,
  deps = _ => second("a"),
  body = a => println("Third task, with params: %s" format a.mkString(", ")))
