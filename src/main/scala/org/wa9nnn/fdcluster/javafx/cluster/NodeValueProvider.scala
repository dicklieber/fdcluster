package org.wa9nnn.fdcluster.javafx.cluster

import org.wa9nnn.fdcluster.javafx.ValuesForNode

trait NodeValueProvider {
  def collectNamedValues(namedValueCollector: ValuesForNode):Unit
}
