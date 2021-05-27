package org.wa9nnn.fdcluster.javafx.cluster

import org.wa9nnn.fdcluster.javafx.cluster.PropertyNames.rowNames

object PropertyNames {
  val rowNames: List[ValueName] =  ValueName.values().toList

  val colHeaderName: ValueName = rowNames.head
}
