package org.wa9nnn.fdcluster.store.network.multicast

import org.wa9nnn.fdcluster.store.JsonContainer

trait JsonContainerSender {
  def send(jsonContainer: JsonContainer): Unit
}
