
package org.wa9nnn.util

import com.sun.javafx.application.PlatformImpl
import org.specs2.mutable.BeforeAfter
import scalafx.application.Platform

trait ScalafxFixture extends BeforeAfter{
  override def before: Any = {
    // We need to have saclafx running becuse the
    // control creation need it to work.
    Platform.startup(new Runnable {
      override def run(): Unit = {
        // don't need to actuall run anytin, but javafx gets started/
      }
    })
  }

  override def after: Any = {
    PlatformImpl.exit()
  }

}
