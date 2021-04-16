package org.wa9nnn.fdcluster.javafx.sync

import org.wa9nnn.fdcluster.http.DestinationActor
import org.wa9nnn.fdcluster.model.{DistributedQsoRecord, QsoRecord}
import org.wa9nnn.fdcluster.model.MessageFormats.Uuid

trait UuidContainer extends ResponseMessage {
  val uuids: List[Uuid]

  def iterator: Iterator[Uuid] = {
    uuids.iterator
  }
  val destination = DestinationActor.cluster
}

trait QsoContainer extends ResponseMessage {
  val qsos: List[QsoRecord]

  def iterator: Iterator[QsoRecord] = {
    qsos.iterator
  }
  val destination = DestinationActor.qsoStore
}

trait ResponseMessage {
  val destination: DestinationActor
}
