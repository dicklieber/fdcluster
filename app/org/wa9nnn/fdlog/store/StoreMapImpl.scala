/*
 * Copyright (c) 2017 HERE All rights reserved.
 */
package org.wa9nnn.fdlog.store

import java.util.UUID
import javax.inject.Inject

import org.wa9nnn.fdlog.model.Contact.WorkedCallsign
import org.wa9nnn.fdlog.model._
import org.wa9nnn.fdlog.util.StructuredLogging
import NodeInfo.Node

import scala.collection.concurrent.TrieMap

class StoreMapImpl @Inject()(implicit nodeInfo: NodeInfo) extends Store with StructuredLogging {
  implicit val node: Node = nodeInfo.node
  private var contacts = new TrieMap[UUID, Contact]()
  private val dups = new TrieMap[WorkedCallsign, Set[Contact]]

  def length: Int = contacts.size

  def add(contact: Contact): Unit = {
    contacts.putIfAbsent(contact.uuid, contact)

  }


  /**
    * Add this onctact iof not a dup.
    *
    * @param potentialContact that may be added.
    * @return None if added, otherwise [[Contact]] that this is a dup of.
    */
  override def add(potentialContact: PotentialContact): Option[Contact] = {
    findDup(potentialContact) match {
      case dup@Some(_) =>
        dup
      case None =>
        val contact = potentialContact.toContact(nodeInfo)
        add(contact)
        Some(contact)
    }
  }

  def findDup(potentialContact: PotentialContact): Option[Contact] = {
    for {
      contacts <- dups.get(potentialContact.workedStation)
      dup ← contacts.find(_.band == potentialContact.band)
    } yield {
      dup
    }
  }

  override def search(in: String): Seq[Contact] = {
    contacts.values.find(_.workedStation.contains(in))
  }.toSeq

  override def dump: Seq[Contact] = contacts.values.toSeq.sorted

  /**
    *
    * @return ids of all [[Contacts]] known to this node.
    */
  def contactIds: NodeUuids = {
    NodeUuids(contacts.keys.toSet)
  }

  def requestContacts(contactRequest: ContactRequest): Contacts = {

    val selectedContacts = if (contactRequest.contactIds.isEmpty) {
      contacts.values
    } else {
      contactRequest.contactIds.flatMap(contacts.get(_))
    }
    Contacts(selectedContacts.toSeq)
  }

  def merge(contactFromAnotherNode: Contacts): Unit = {

    contactFromAnotherNode.contacts.foreach { contact ⇒ {
      val maybeExisting = contacts.putIfAbsent(contact.uuid, contact)
      if (logger.isDebugEnabled) {
        (maybeExisting match {
          case None ⇒
            logJson("merged")
          case Some(_) ⇒
            logJson("exists")

        })
          .field("uuid", contact.uuid)
          .field("worked", contact.workedStation)
          .debug()
      }
    }
    }
  }
}