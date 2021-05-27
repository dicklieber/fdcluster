package org.wa9nnn.fdcluster.javafx.cluster

import scala.collection.immutable.HashSet


trait NodeValueProvider {
  def collectNamedValues(namedValueCollector: NamedValueCollector): Unit
}

class NamedValueCollector {
  private val builder = HashSet.newBuilder[NamedValue]

  def apply(key: PropertyCellName, value: Any): Unit = {
    builder += (NamedValue(key, value))
  }

  def result: Set[NamedValue] = {
    builder.result()
  }
}