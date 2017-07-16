/*
 * Copyright (c) 2017 HERE All rights reserved.
 */
package org.wa9nnn.fdlog.store

import org.wa9nnn.fdlog.model.Contact

class StoreMapIMpl extends Store {

  var contacts: Seq[Contact] = Seq.empty

  def length: Int = contacts.length

  override def add(contact: Contact): Unit = {
    contacts = contact +: contacts
  }

  override def search(in: String): Seq[Contact] = {
    contacts.filter { contact ⇒
      contact.workedStation.contains(in) ||
        contact.exchange.contains(in)
    }
  }

  override def dump: Seq[Contact] = contacts

  def
}