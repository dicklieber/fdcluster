
package org.wa9nnn.fdcluster.javafx.entry

import com.google.inject.name.Named
import org.wa9nnn.fdcluster.model._
import _root_.scalafx.beans.binding.Bindings
import _root_.scalafx.beans.property.ObjectProperty

import javax.inject.{Inject, Singleton}
@Singleton
class BandModeQsoMetadata @Inject()(
                                    nodeAddress: NodeAddress,
                                    @Named("currentStation") currentStationProperty: ObjectProperty[CurrentStation],
                                    contestPropertyObject: ContestProperty) {

//  private val contestProperty: ObjectProperty[Contest] = contestPropertyObject.contestProperty
//
//  val b = Bindings.createObjectBinding(() => {
//    val currentStation = currentStationProperty.value
//    QsoMetadata(operator = currentStation.operator,
//      rig = currentStation.rig,
//      ant = currentStation.antenna,
//      node = nodeAddress.display,
//      contestId = contestProperty.value.toId
//    )
//  }, currentStationProperty, contestProperty)
//

}
