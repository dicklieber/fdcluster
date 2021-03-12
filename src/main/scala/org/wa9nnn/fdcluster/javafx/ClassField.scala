
package org.wa9nnn.fdcluster.javafx


import org.wa9nnn.fdcluster.javafx.entry.ContestClassValidator
import org.wa9nnn.util.WithDisposition
import scalafx.scene.control.TextField

/**
 * Callsign entry field
 * sad or happy as validated while typing.
 *
 */
class ClassField extends TextField with WithDisposition with NextField {
  setFieldValidator(ContestClassValidator)

  validProperty.onChange{(_,_,nv) =>
    if(nv) {
      // move to next field as soon as class is vallid.
      onDoneFunction("")
    }
  }

}

