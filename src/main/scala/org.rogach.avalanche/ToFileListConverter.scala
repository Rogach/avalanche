package org.rogach.avalanche

import java.io.File

trait ToFileListConverter[A] {
  def convertToFileList(a: A): List[File]
}

object ToFileListConverter {
  implicit val unitToFileListConverter = new ToFileListConverter[Unit] {
    def convertToFileList(u: Unit) = Nil
  }
  implicit val fileToFileListConverter = new ToFileListConverter[File] {
    def convertToFileList(file: File) = List(file)
  }
  implicit val stringToFileListConverter = new ToFileListConverter[String] {
    def convertToFileList(s: String) = List(f(s))
  }
}
