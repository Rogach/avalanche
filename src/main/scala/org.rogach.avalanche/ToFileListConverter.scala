package org.rogach.avalanche

import java.io.File

trait ToFileListConverter[A] {
  def convertToFileList(a: A): List[File]
}
