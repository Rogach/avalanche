var i = 1
task("default",
  rerun = _ => i > 0, // so that this test build will pass
  deps = _ => Nil,
  body = _ => {
    exec("./run.sh")
    i -= 1
  })
