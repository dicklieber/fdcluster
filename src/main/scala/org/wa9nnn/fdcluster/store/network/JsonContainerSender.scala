package org.wa9nnn.fdcluster.store.network

import org.wa9nnn.fdcluster.store.JsonContainer


trait JsonContainerSender {
  def send(jsonContainer: JsonContainer):Unit
}