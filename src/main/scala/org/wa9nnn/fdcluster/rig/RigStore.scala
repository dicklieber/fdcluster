
package org.wa9nnn.fdcluster.rig

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.wa9nnn.util.JsonLogging
import scalafx.beans.property.{IntegerProperty, ObjectProperty, StringProperty}

import java.util.prefs.Preferences
import javax.inject.Inject

/**
 * Manages Rig information
 *
 * @param preferences default or runtime, override with a mock Preferences for unit testing
 */
class RigStore @Inject()(preferences: Preferences = Preferences.userRoot.node("org/wa9nnn/fdcluster")) extends JsonLogging {

  val rigFrequencyDisplay: StringProperty = new StringProperty()
  val rigFrequency = new IntegerProperty()

  rigFrequency.onChange { (ov, was, tobo) =>
    rigFrequencyDisplay.set(f"${tobo.intValue() / 1000000 % .04}")
  }

  val rigMode: StringProperty = StringProperty("None")

  val rigSettings: ObjectProperty[RigSettings] = new ObjectProperty[RigSettings]()

  private val prefsKey = "rigSettings"
  rigSettings.value = {
    val str = preferences.get(prefsKey, "")
    try {
      RigSettings.decodeJson(str)
    } catch {
      case e:MismatchedInputException =>
        RigSettings()
    }
  }
  rigSettings.onChange { (_, _, newSettings) =>
    preferences.put(prefsKey, newSettings.encodeJson)
  }

}



