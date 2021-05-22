package org.wa9nnn.fdcluster.store

import org.wa9nnn.fdcluster.model.StationProperty

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class DupQsoDetector @Inject()(stationProperty: StationProperty, storeSender: StoreSender) {

  import scala.concurrent.ExecutionContext.Implicits.global

  def apply(partialCallSign: String): Future[SearchResult] = {
    val future: Future[SearchResult] = storeSender ? [SearchResult]Search(partialCallSign, stationProperty.value.bandMode)
    future
  }
}
