package org.wa9nnn.fdcluster

import java.time.Instant

trait BuildInfoBase {
  val builtAtMillis: Long
  val version: String

  lazy val buildInstant: Instant = Instant.ofEpochMilli(builtAtMillis)
  private val verparser = """(\d+)\.(\d+).(\d+)-?(.+)?""".r
  /**
   * Does not include patch.
   * This is included in most messages that are sent between nodes
   * so we can detect miss-matches before weird stuff happens.
   *
   */
  lazy val canonicalVersion:String = {
    lazy val verparser(major, minor, patch, snapshot) = version
    s"$major.$minor"
  }
}
