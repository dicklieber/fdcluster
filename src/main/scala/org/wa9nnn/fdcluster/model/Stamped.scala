package org.wa9nnn.fdcluster.model

import java.time.Instant

/**
 * Something that has stamp and can be tested for newness.
 *
 */
trait Stamped[T <: Stamped[T]] extends Product{
  val stamp: Instant


}