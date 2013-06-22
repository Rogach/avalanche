onInit {
  println("init")
}
val default = aggregate("default", List(second("a"), third))
val second = task("second",
  rerun = once,
  deps = _ => Nil,
  body = a => println("Second task, with params: %s" format a.mkString(", ")))
val third = task("third",
  rerun = once,
  deps = _ => second("a"),
  body = a => println("Third task, with params: %s" format a.mkString(", ")))
