
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

import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Udp}
import akka.util.ByteString
import com.google.inject.name.Named
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import nl.grons.metrics4.scala.DefaultInstrumented
import org.wa9nnn.fdcluster.NetworkControl
import org.wa9nnn.fdcluster.store.JsonContainer

import java.net.InetSocketAddress
import javax.inject.{Inject, Singleton}

class MultcastSenderActor(val config: Config,
                          clusterControl: NetworkControl) extends Actor with MulticastActor with DefaultInstrumented with LazyLogging {

  import context.system

  IO(Udp) ! Udp.SimpleSender

  def receive: PartialFunction[Any, Unit] = {
    case Udp.SimpleSenderReady =>
      context.become(ready(sender()))
  }

  def ready(send: ActorRef): Receive = {
    case something: JsonContainer =>
      val bytes: Array[Byte] = something.bytes
      logger.whenTraceEnabled {
        logger.trace(s"Sending: $something to $multicastGroup:$port")
      }
      if (clusterControl.isUp)
        send ! Udp.Send(ByteString(bytes), new InetSocketAddress(multicastGroup, port))

    case x ⇒
      logger.error(s"MulticastSenderActor: Unexpected: $x")
  }
}

@Singleton
class MulticastSender @Inject()(@Named("multicastSender") multicastSender: ActorRef) {
  def !(jsonContainer: JsonContainer): Unit = {
    multicastSender ! jsonContainer
  }
}