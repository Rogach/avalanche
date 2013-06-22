val fls = task("files",
  inputs = files("%s.txt"),
  outputs = files("%s.txt.out"),
  body = { args =>
    import sys.process._
    Seq("cp", "%s.txt" format args.head, "%s.txt.out" format args.head) !
  })


aggregate("default", List(fls("a")))
