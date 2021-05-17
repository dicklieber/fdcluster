package org.wa9nnn.fdcluster.javafx.debug

import org.wa9nnn.fdcluster.store.StoreSender

import javax.inject.{Inject, Singleton}

@Singleton
class Reseter @Inject()(storeSender: StoreSender) {
  def reset() = {
    // stop entry
    // stop cluster access
    // rename directory tp <diretory>.<filestamp>
    // ClearStore
    // enable cluser access
  }
}
