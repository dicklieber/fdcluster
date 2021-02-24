
package org.wa9nnn.fdcluster.adif

case class Adif(header: Header, records: Seq[Record])

case class Header(preHeader: Option[String] = None, fields: Seq[Field]= Seq.empty) //, adifVersion: String = "3.0.9"

case class Record(preRecord: Option[String], fields: Seq[Field])


