package org.wa9nnn.fdcluster.model

import com.typesafe.scalalogging.LazyLogging
import org.wa9nnn.fdcluster.FileContext
import org.wa9nnn.fdcluster.contest.OkToLogContributer
import play.api.libs.json.{Format, Writes}
import scalafx.beans.property.ObjectProperty

import java.nio.file.Files
import scala.reflect.ClassTag

abstract class PersistableProperty[T <: Stamped[_] : ClassTag](fileContext: FileContext)(implicit reads: Format[T])
  extends ObjectProperty[T] with OkToLogContributer with LazyLogging {
  /**
   * provide a new default instance of T. Needed when there is no file persisted/
   *
   * @return
   */
  def defaultInstance: T

  /**
   * Invoked initially and when the property changes.
   */
  def onChanged(v: T): Unit

  def maybeValue:Option[T] = {
    if (okToLogProperty.value) {
      Option(value)
    } else {
      None
    }
  }

  /**
   * If the candidate is newer than the current value then persist the new value, update the property
   *
   */
  def update(candidate: T)(implicit writes: Writes[T]): Unit = {
    if(candidate.stamp.isAfter( value.stamp) || !Files.exists (fileContext.pathForClass[T]))
      fileContext.saveToFile(candidate)

    super.update(candidate)
  }

  val init: T = fileContext.loadFromFile[T] {
    () => defaultInstance
  }
  value = init
  onChanged(init)

  onChange {
    (_, _, nv) =>
      onChanged(nv)
  }

}

