/*
 * Copyright (c) 2017 HERE All rights reserved.
 */
package org.wa9nnn.fdlog.store

import org.wa9nnn.fdlog.model.{Contact, Contacts, NodeUuids, PotentialContact}

trait Store {

  /**
    * Add this onctact iof not a dup.
    *
    * @param potentialContact that may be added.
    * @return None if added, otherwise [[Contact]] that this is a dup of.
    */
  def add(potentialContact: PotentialContact): Option[Contact]


  def search(in: String): Seq[Contact]

  /**
    *
    * @return ids of all [[Contacts]] known to this node.
    */
  def contactIds: NodeUuids

  def dump: Seq[Contact]
}
