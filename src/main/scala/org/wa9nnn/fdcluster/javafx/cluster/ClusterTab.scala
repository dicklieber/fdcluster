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

package org.wa9nnn.fdcluster.javafx.cluster

import com.google.inject.Inject
import scalafx.scene.control.{ScrollPane, Tab}

/**
 * Create JavaFX UI to view status of each node in the cluster.
 */
class ClusterTab @Inject()(clusterTable: ClusterTable) extends Tab {

  text = "Cluster"
  private val scrollPane = new ScrollPane()
  scrollPane.content= clusterTable
  content = scrollPane
  closable = false


}
