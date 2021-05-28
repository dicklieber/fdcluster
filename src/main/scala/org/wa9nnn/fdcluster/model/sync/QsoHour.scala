
/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.fdcluster.model.sync

import com.wa9nnn.util.tableui.Cell
import org.scalafx.extras.onFX
import org.wa9nnn.fdcluster.javafx.cluster.{PropertyCell, PropertyCellName, SimplePropertyCell}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.fdcluster.model.{Qso, sync}
import org.wa9nnn.fdcluster.store.network.FdHour
import scalafx.scene.control.Label
import scalafx.scene.layout.{AnchorPane, BorderPane, HBox}

import java.security.MessageDigest

/**
 *
 * @param fdHour      hour this is for..
 * @param qsos        QSOs in this hour.
 */
case class QsoHour(fdHour: FdHour, qsos: List[Qso]) {

  import org.wa9nnn.util.UuidUtil._

  lazy val hourDigest: QsoHourDigest = {
    val messageDigest: MessageDigest = MessageDigest.getInstance("SHA-256")
    qsos.foreach(qr ⇒ messageDigest.update(qr.uuid))
    val bytes = messageDigest.digest()
    val encoder = java.util.Base64.getEncoder
    val bytes1 = encoder.encode(bytes)
    val sDigest = new String(bytes1)
    sync.QsoHourDigest(fdHour, sDigest, qsos.size)
  }

  lazy val qsoIds: QsoHourIds = {
    val ids = qsos.map(_.uuid)
    QsoHourIds(fdHour, ids)
  }

  override def toString: Node = {
    super.toString
  }

}

object QsoHour {
  def apply(qsos: List[Qso]): QsoHour = {
    assert(qsos.nonEmpty, "Must have some qsos in an hour.")
    val startOfHour = qsos.head.fdHour
    QsoHour(startOfHour, qsos)
  }
}

/**
 * Used to quickly compare one node's hour with another.
 *
 * @param fdHour      truncated to the hour.
 * @param digest      of all the QsoIDs in this hour.
 * @param size        number of Qsos in this hour.  //todo Do we actually need this? isn't the digest sufficient?
 */
case class QsoHourDigest(fdHour: FdHour, digest: Digest, size: Int)  extends PropertyCellName {

  override def toString: Node = {
    super.toString
  }

  def toCell: Cell = {
    if (size == 0) {
      Cell("--")
        .withToolTip("No QSOs for this hour.")
    } else {
      Cell(s"$size: ${DigestFormat(digest)}")
        .withToolTip("Qso Count: ${qsoHourDigest.size}\ndigest: ${qsoHourDigest.digest}\nDigest is based on all the QSO UUIDs in the hour.")
    }
  }

  val toolTip: String = "Number of QSOs and digest for the hour."

  val name: String = fdHour.name
  def PropertyCell:QsoDigestPropertyCell = QsoDigestPropertyCell(this)
}

case class QsoHourIds(startOfHour: FdHour, qsiIds: List[Uuid])

object DigestFormat {
  def apply(digest: Digest): String = {
    digest.take(10) + "..."
  }
}

case class QsoDigestPropertyCell(initialValue: QsoHourDigest) extends BorderPane with PropertyCell[QsoHourDigest] {
  var current: QsoHourDigest = initialValue
  prefWidth = 150.0
  val countLabel: Label = new Label(){
    styleClass += "number"
  }

  right = countLabel
  styleClass  ++= Seq("clusterCell" , "number")


  update(initialValue)

   def update(qsoHourDigest: QsoHourDigest): Unit = {
     current = qsoHourDigest
    onFX{
      countLabel.text =  Cell(qsoHourDigest.size).value
    }
  }
}