package org.wa9nnn.util

object OsSelect {
  def apply[T](ifWindows: T, ifUnixLike: T): T = {
    if (System.getProperty("os.name").toLowerCase.contains("win"))
      ifWindows
    else
      ifUnixLike
  }
}

