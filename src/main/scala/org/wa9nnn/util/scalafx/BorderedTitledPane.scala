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

package org.wa9nnn.util.scalafx

import scalafx.geometry.Pos
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.layout.StackPane

case class BorderedTitledPane( title: String, content: Node) extends StackPane {
  alignment = Pos.TopCenter
  val titleLabel: Label = new Label(" " + title + " ") {
    styleClass += "bordered-titled-title"
  }
  val contentPane = new StackPane()
  content.getStyleClass.add("bordered-titled-content")
  contentPane.children += content
  styleClass.add("bordered-titled-border")
  children ++= Seq(titleLabel, contentPane)

}


/*
class BorderedTitledPane extends StackPane {
  BorderedTitledPane(String titleString, Node content) {
    Label title = new Label(" " + titleString + " ");
    title.getStyleClass().add("bordered-titled-title");
    StackPane.setAlignment(title, Pos.TOP_CENTER);

    StackPane contentPane = new StackPane();
    content.getStyleClass().add("bordered-titled-content");
    contentPane.getChildren().add(content);

    getStyleClass().add("bordered-titled-border");
    getChildren().addAll(title, contentPane);
  }
}
 */