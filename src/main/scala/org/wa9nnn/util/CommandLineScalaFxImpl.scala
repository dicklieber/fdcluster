
package org.wa9nnn.util

import scalafx.application.JFXApp.Parameters

class CommandLineScalaFxImpl(parameters: Parameters) extends CommandLine {
  val noArg: Set[String] = parameters.unnamed.map(_.dropWhile(_ == '-')).toSet

  /**
   *
   * @param name of command line parameter without leading dashes.
   * @return true if
   */
  def is(name: String): Boolean = {
    noArg.contains(name)
  }

  def getString(name: String): Option[String] = {
    parameters.named.get(name)
  }

  override def getInt(name: String): Option[Int] = {
    getString(name).map(_.toInt)
  }
}

trait CommandLine {
  def is(name: String): Boolean

  def getString(name: String): Option[String]

  def getInt(name: String): Option[Int]
}