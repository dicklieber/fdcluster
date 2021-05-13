
/*
 * Copyright (C) 2021  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.wa9nnn.fdcluster.store.network

import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Udp}
import akka.util.ByteString
import com.typesafe.config.Config
import nl.grons.metrics4.scala.DefaultInstrumented
import org.wa9nnn.fdcluster.ClusterControl
import org.wa9nnn.fdcluster.store.JsonContainer

import java.net.InetSocketAddress

class MultcastSenderActor(val config: Config,
                          clusterControl: ClusterControl) extends Actor with MulticastActor with DefaultInstrumented {

  import context.system

  IO(Udp) ! Udp.SimpleSender

  def receive: PartialFunction[Any, Unit] = {
    case Udp.SimpleSenderReady =>
      context.become(ready(sender()))
  }

  def ready(send: ActorRef): Receive = {
    case something: JsonContainer =>
      val bytes: Array[Byte] = something.bytes
      whenTraceEnabled { () =>
        val str = new String(bytes)
        s"Sending: $something to $multicastGroup:$port"
      }
      if (clusterControl.isUp)
        send ! Udp.Send(ByteString(bytes), new InetSocketAddress(multicastGroup, port))

    case x ⇒
      logger.error(s"MulticastSenderActor: Unexpected: $x")
  }
}

