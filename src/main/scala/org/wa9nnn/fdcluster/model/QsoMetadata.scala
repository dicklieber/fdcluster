
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

package org.wa9nnn.fdcluster.model

import org.wa9nnn.fdcluster.BuildInfo
import org.wa9nnn.fdcluster.javafx.NamedCellProvider
import org.wa9nnn.fdcluster.model.MessageFormats.CallSign


/**
 *
 * @param operator  who is using app. callSign
 * @param rig       free form usually transceiver model.
 * @param ant       free form antenna description.
 * @param node      what node, in the cluster this came from.
 * @param contestId so old data can't accident be missed with current.
 * @param v         FDCLuster Version that built this so we can detect mismatched versions.
 */
case class QsoMetadata(operator: CallSign = "",
                       rig: String = "",
                       ant: String = "",
                       node: String = "localhost;1",
                       contestId: String = "FD2021WA9NNN",
                       v: String = BuildInfo.canonicalVersion) extends NamedCellProvider {

}

