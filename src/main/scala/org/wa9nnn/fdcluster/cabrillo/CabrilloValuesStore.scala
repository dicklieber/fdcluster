
package org.wa9nnn.fdcluster.cabrillo

import com.wa9nnn.cabrillo.model.{SimpleTagValue, TagValue}
import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.{JsonLogging, Persistence}
import scalafx.beans.property.ObjectProperty

import javax.inject.Inject

class CabrilloValuesStore @Inject()(preferences: Persistence) extends ObjectProperty[CabrilloExportRequest] with JsonLogging {
  value = preferences.loadFromFile[CabrilloExportRequest].getOrElse(CabrilloExportRequest())

  onChange { (_, _, newValue) =>
    preferences.saveToFile(newValue)
  }
}

case class CabrilloExportRequest(directory: String = System.getProperty("user.home"), fileName: String = "", cabrilloValues: CabrilloValues = CabrilloValues())

case class CabrilloValue(name: String, value: String) {
  def tagValue: TagValue = SimpleTagValue(name, 0, value)
}

case class CabrilloValues(fieldValues: Seq[CabrilloValue] = Seq.empty) {
  private lazy val map = fieldValues.map(v =>
    v.name -> v.value).toMap

  def valueForName(name: String): Option[String] =
    map.get(name)
}
