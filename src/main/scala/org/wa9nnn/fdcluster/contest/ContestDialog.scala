
/*
 * Copyright © 2021 Dick Lieber, WA9NNN
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
 */

package org.wa9nnn.fdcluster.contest

import _root_.scalafx.Includes._
import _root_.scalafx.scene.control._
import _root_.scalafx.scene.layout.HBox
import com.google.inject.Injector
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector

import javax.inject.Inject

/**
 * UI for things that need to be setup for the contest.
 *
 */
class ContestDialog @Inject()(injector: Injector) extends Dialog[Contest] with LazyLogging {
  val dp: DialogPane = dialogPane()

  private val cancelButton = ButtonType.Cancel


  dp.getButtonTypes.addAll(cancelButton)
  dp.getStylesheets.addAll(
    getClass.getResource("/com/sun/javafx/scene/control/skin/modena/modena.css").toExternalForm,
    getClass.getResource("/fdcluster.css").toExternalForm
  )


  dialogPane().setContent(new HBox(
    injector.instance[ContestDialogPane].pane,
    injector.instance[JournalDialogPane].pane
  ))
}

