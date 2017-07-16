/*
 * Copyright (c) 2017 HERE All rights reserved.
 */
package org.wa9nnn.fdlog.store

import org.wa9nnn.fdlog.model.Contact

trait Store {
  def add(contact: Contact)

  def search(in: String): Seq[Contact]

  def dump:Seq[Contact]
}
