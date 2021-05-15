package org.wa9nnn.fdcluster.store

import org.wa9nnn.fdcluster.model.QsoRecord

case class ImportProblem private (candidate: QsoRecord, existing:QsoRecord, reason:String)

