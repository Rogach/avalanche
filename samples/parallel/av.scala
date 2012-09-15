def wait(s: String) = { (_:List[String]) =>
  log("Started %s!" format s)
  Thread.sleep(1000)
  log("Ended %s." format s)
}

val default = task("default",
  rerun = once, 
  deps = _ => Seq(a,b),
  body = wait("default"))
val a = task("a",
  rerun = once,
  deps = _ => Seq(d, c),
  body = wait("a"))
val b = task("b",
  rerun = once,
  deps = _ => c,
  body = wait("b"))
val c = task("c",
  rerun = once,
  deps = nodeps,
  body = wait("c"))
val d = task("d",
  rerun = once,
  deps = nodeps,
  body = wait("d"))
  
a.threadAmount = 3
b.threadAmount = 2
