
package org.wa9nnn.fdcluster.rig

import org.wa9nnn.fdcluster.model.MessageFormats._
import org.wa9nnn.util.{JsonLogging, Persistence}
import scalafx.beans.property.{IntegerProperty, ObjectProperty, StringProperty}

import javax.inject.Inject

/**
 * Manages Rig information
 *
 * @param preferences default or runtime, override with a mock Preferences for unit testing
 */
class RigStore @Inject()(persistence: Persistence) extends JsonLogging {

  val rigFrequencyDisplay: StringProperty = new StringProperty()
  val band:StringProperty = new StringProperty()
  val rigFrequency = new IntegerProperty()

  rigFrequency.onChange { (ov, was, tobo) =>
    rigFrequencyDisplay.set(f"${tobo.intValue() / 1000000 % .04}")
  }

  val rigMode: StringProperty = StringProperty("None")

  val rigSettings: ObjectProperty[RigSettings] = new ObjectProperty[RigSettings]()

  private val prefsKey = "rigSettings"
  rigSettings.value = {
    persistence.loadFromFile[RigSettings].getOrElse(RigSettings())
  }
  rigSettings.onChange { (_, _, newSettings) =>
    persistence.saveToFile(rigSettings.value)
  }

}



