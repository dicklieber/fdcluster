
package org.wa9nnn.fdcluster.javafx

import org.wa9nnn.fdcluster.model.MessageFormats.CallSign
import org.wa9nnn.util.HappySad
import scalafx.beans.property.ObjectProperty
import scalafx.scene.control.TextField

/**
 * Callsign entry field
 * sad or happy as validated while typing.
 *
 */
class CallSignField extends TextField with HappySad {
 val callSignProperty = new ObjectProperty[Option[CallSign]]()

  onKeyTyped = {_ =>
    callSignProperty .value = ContestCallsignValidator.valid(text.value)
  }

}
