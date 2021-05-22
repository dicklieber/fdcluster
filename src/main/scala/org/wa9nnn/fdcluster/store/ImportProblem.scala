package org.wa9nnn.fdcluster.store

import org.wa9nnn.fdcluster.model.Qso

case class ImportProblem private (candidate: Qso, existing:Qso, reason:String)

