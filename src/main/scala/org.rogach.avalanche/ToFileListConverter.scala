package org.rogach.avalanche

import java.io.File

trait ToFileListConverter[A] {
  def convertToFileList(a: A): List[File]
}

object ToFileListConverter {
  implicit val unitToFileListConverter = new ToFileListConverter[Unit] {
    def convertToFileList(u: Unit) = Nil
  }
  implicit val unitListToFileListConverter = new ToFileListConverter[Seq[Unit]] {
    def convertToFileList(u: Seq[Unit]) = Nil
  }

  implicit val stringToFileListConverter = new ToFileListConverter[String] {
    def convertToFileList(s: String) = List(new File(s))
  }
  implicit val stringSeqToFileListConverter = new ToFileListConverter[Seq[String]] {
    def convertToFileList(fileNames: Seq[String]) = fileNames.map(new File(_)).toList
  }
  implicit val stringListToFileListConverter = new ToFileListConverter[List[String]] {
    def convertToFileList(fileNames: List[String]) = fileNames.map(new File(_))
  }

  implicit val fileToFileListConverter = new ToFileListConverter[File] {
    def convertToFileList(file: File) = List(file)
  }
  implicit val fileSeqToFileListConverter = new ToFileListConverter[Seq[File]] {
    def convertToFileList(files: Seq[File]) = files.toList
  }
  implicit val fileListToFileListConverter = new ToFileListConverter[List[File]] {
    def convertToFileList(files: List[File]) = files
  }
}
