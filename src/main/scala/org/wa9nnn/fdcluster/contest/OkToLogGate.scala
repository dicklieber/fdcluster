package org.wa9nnn.fdcluster.contest

import scalafx.beans.binding.Bindings
import scalafx.beans.property.BooleanProperty

import javax.inject.{Inject, Singleton}
import scala.collection.immutable

/**
 * Binds to all the proerties that contribute to it being ok to log.
 *
 * @param okToLogContributors that we will consider to determine if logging is ok.
 */
@Singleton
class OkToLogGate @Inject()(okToLogContributors: immutable.Set[OkToLogContributer]) extends BooleanProperty {
  private def ok = {
    val bool = okToLogContributors.forall(_.okToLogProperty.value.booleanValue())
    bool
  }
  value = ok

  this <== Bindings.createBooleanBinding(
    () =>
      ok,
    okToLogContributors.map(_.okToLogProperty).toSeq: _*)


}
