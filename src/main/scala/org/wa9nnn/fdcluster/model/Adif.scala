
package org.wa9nnn.fdcluster.model

import java.time.Instant

case class Adif(header: Header, records: Seq[Record])

case class Header(text: String, created: Instant, programId: String, programVersion: String, userText: Seq[String]) //, adifVersion: String = "3.0.9"

case class Record(name: String, value: String)