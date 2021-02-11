
package org.wa9nnn.fdcluster.rig

/**
 * Low level interface to HamLib
 */
trait Rig {
  def frequency: Int

  def modeAndBandWidth: (String,Int)

  def caps:Map[String,String]

  def radio:String

}
