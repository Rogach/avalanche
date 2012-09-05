AVALANCHE
=========

A simple, file-concerned build system. It uses .scala file as general configuration and code you will need to carry out your tasks.

Avalanche gives you convenient way to specify your task tree using full power of Scala language. 
Unlike other buildsystems (e.g. SBT) it allows single task to be run several times per build (with different params), allows dependencies to all tasks depend on input parameters,
and it is completely flexible - you have fine control over execution of every task. For example, the following is a simple build definition for transforming of some file:

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

For convenience, Avalanche also has utilities to test the need to rerun using the modification status of input and output files.

Requirements
============

* JRE (1.6 or newer)

Installation
============

You can download avalanche-{version}.jar from github site - it is an executable jar and does not require any other files.

For convenience, you can use the following script to launch avalanche (name it "av", make it executable, and place somewhere on your PATH):

    java -jar avalanche-{version}.jar "$@"

Usage
=====

    av [option]... [tasks]...

Configuration
=============
## Command line options

     av [OPTION]... [TASK]...
   
     -b, --build-file  <arg>   use the given file instead of default build.av or 
                               build.av.xml 
     -D, --dry-run             only list the tasks in order of their execution, do 
                               not build anything 
     -f, --force  <tasks>...   force several tasks to re-build (with parameters) 
     -F, --force-all           force all depended tasks to be rebuilded 
     -q, --quiet               supress avalanche output 
     -s, --silent              supress all output, including output from scripts 
                               (stderr from scripts is still printed) 
     -S, --supress  <arg>...   tasks to be supressed 
     -t, --tasks  <arg>...     tasks to run 
     -v, --verbose             print more information 
   
    trailing arguments:
     tasks to run (required)   tasks to run 
