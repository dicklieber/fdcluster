
package org.wa9nnn.fdcluster.javafx.cluster


trait Row {
  def rowHeader: StyledAny

  def cells: Seq[StyledAny]

}

/**
 *
 * @param rowHeader name show in 1st column of row.
 * @param cells     things that an be rendered.
 */
case class MetadataRow(rowHeader: StyledAny, cells: Seq[StyledAny]) extends Row
