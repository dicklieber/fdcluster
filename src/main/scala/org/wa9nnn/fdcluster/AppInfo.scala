package org.wa9nnn.fdcluster

import java.time.Instant
import javax.inject.Singleton

@Singleton
class AppInfo {
  val started: Instant =  Instant.now()
}
