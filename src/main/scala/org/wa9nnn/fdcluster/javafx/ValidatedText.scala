
package org.wa9nnn.fdcluster.javafx

import org.wa9nnn.fdcluster.javafx.entry.{AlwaysValid, FieldValidator}
import org.wa9nnn.util.HappySad
import scalafx.scene.control.{Label, TextField}

class ValidatedText(validator: FieldValidator = AlwaysValid) extends TextField with HappySad with Validatable {
  var errLabel: Label = new Label("")
//  errLabel.setPrefWidth(150)
  errLabel.styleClass += "sad"
  this.focusedProperty.addListener { (_, had, _) => {
    if (had) {
      validator.valid(text.value)
      validator.valid(text.value) match {
        case Some(error) =>
          sad()
          errLabel.setText(error)
        case None =>
          happy()
          errLabel.setText("")
      }
    }
  }
  }

  override def validate(): Option[String] = {
    validator.valid(text.value)
  }
}

trait Validatable {
  def validate(): Option[String]
}