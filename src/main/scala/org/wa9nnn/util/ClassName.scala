package org.wa9nnn.util

object ClassName {
  def last(clazz: Class[_]): String = {
    clazz.getName.split("""\.""").last
  }
}
