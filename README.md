AVALANCHE
=========

A simple, file-concerned build system. It uses script .scala file as configuration.

Avalanche gives you convenient way to specify your task tree using full power of Scala language. 
Unlike other buildsystems (e.g. SBT) it allows single task to be run several times per build (with different params), allows dependencies to all tasks depend on input parameters,
and it is completely flexible - you have fine control over execution of every task. For example, the following is a simple build definition for transforming of some files:

```scala
import java.io.File
task("default",
  rerun = fname => new File(fname + ".out").exists,
  deps = _ => Nil,
  body = { fname =>
    // process the file, put the result into "fname.out" file
  })
```

You will be able to run this example with `av default[aoeu.txt]` command.

For convenience, Avalanche also has utilities to test the need to rerun using the modification status of input and output files:

```scala
task("default",
  inputs = files("%s.txt"), // %s will be replaced with the first task parameter
  outputs = files("%s.txt.out"),
  body = { args =>
    import sys.process._
    Seq("cp", "%s.txt" format args.head, "%s.txt.out" format args.head) !
  })
```

In this example, if you execute `av default[a]`, Avalanche would check modification times of `a.txt` and `a.txt.out`, and if `a.txt` is newer or `a.txt.out` does not exist, it would run the task.

For examles, you can look at `samples/` directory here at github repository.

Requirements
============

* JRE (1.6 or newer)

Installation
============

Right now, there is no packaged jar for the project - but you can easily build it yourself with the following commands:

```bash
git clone git@github.com:Rogach/avalanche.git
cd avalanche
sbt assembly
```

Now the needed jar is located in `target/` directory - it is an executable jar and does not require any other files.

For convenience, you can use the following script to launch avalanche (name it "av", make it executable, and place somewhere on your PATH):

    java -jar avalanche-{version}.jar "$@"

Usage
=====

    av [option]... [tasks]...
    av --help
