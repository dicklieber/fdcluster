package org.wa9nnn.fdcluster

import com.typesafe.scalalogging.LazyLogging
import scalafx.beans.property.BooleanProperty
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.GridPane

import javax.inject.{Inject, Singleton}
import scala.util.Using

@Singleton
class NetworkPane @Inject()(clusterControl: ClusterControl) extends GridPane {

  private val networkUpDown: ToggleImages = ToggleImages("share-link.png", "broken-link.png", clusterControl:ClusterControl)

  add(networkUpDown, 0, 0)

}

case class ToggleImages(trueImage: String, falseImage: String, which: BooleanProperty) extends ImageView with LazyLogging {
  val image1: Image = Using(getClass.getResourceAsStream(s"/images/$trueImage")) { is =>
    new Image(is)
  }.get
  val image2: Image = Using(getClass.getResourceAsStream(s"/images/$falseImage")) { is =>
    new Image(is)
  }.get
  image = use(which.value)

  which.onChange { (_, _, nv) =>
    image = use(nv)
  }

  def use(which: Boolean):Image = {
    if (which)
      image1
    else
      image2
  }
  def use1(): Unit = image = image1

  def use2(): Unit = image = image2


}