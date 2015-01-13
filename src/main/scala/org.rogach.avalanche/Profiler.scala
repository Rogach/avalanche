package org.rogach.avalanche

import scala.collection.mutable

object Profiler {

  val profileData = new mutable.HashMap[String, mutable.ListBuffer[Long]]()

  def profile[A](name: String)(fn: => A): A = {
    if (Avalanche.opts != null && Avalanche.opts.profile()) {
      val stt = System.currentTimeMillis
      try {
        fn
      } finally {
        val elapsed = System.currentTimeMillis - stt
        if (!profileData.contains(name)) {
          profileData.put(name, new mutable.ListBuffer[Long]())
        }
        profileData(name) += elapsed
      }
    } else {
      fn
    }
  }

  def count(name: String, n: Int = 1) {
    if (Avalanche.opts != null && Avalanche.opts.profile()) {
      if (!profileData.contains(name)) {
        profileData.put(name, new mutable.ListBuffer[Long]())
      }
      (1 to n).foreach { _ =>
        profileData(name) += 0
      }
    }
  }

  def dump() {
    printf("Profile results:\n")
    val names = profileData.keys.toList
    val nameLength = names.map(_.size).max
    printf(s"%${nameLength}s | count | ms/op | ms total |  ms max\n", "")
    profileData.mapValues(_.toList).toList.sortBy(_._1).foreach { case (name, times) =>
      val count = times.size
      val total = times.sum
      val mean = total / times.size
      val max = times.max
      printf(s"%-${nameLength}s | %5d | %5d | %8d | %6d\n", name, count, mean, total, max)
    }
  }

}
