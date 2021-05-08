
/*
 * Copyright © 2021 Dick Lieber, WA9NNN
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
 */

package org.wa9nnn.fdcluster

import _root_.scalafx.application.JFXApp.Parameters
import _root_.scalafx.beans.property.ObjectProperty
import akka.actor.{ActorRef, ActorSystem, DeadLetter, Props}
import com.github.racc.tscg.TypesafeConfigModule
import com.google.inject.{AbstractModule, Injector, Provides}
import com.typesafe.config.ConfigFactory
import configs.Config
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}
import org.wa9nnn.fdcluster.contest.{JournalProperty, JournalPropertyWriting}
import org.wa9nnn.fdcluster.javafx.entry.{RunningTaskInfoConsumer, RunningTaskPane, StatsPane}
import org.wa9nnn.fdcluster.metrics.MetricsReporter
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.model.sync.{ClusterActor, NodeStatusQueueActor}
import org.wa9nnn.fdcluster.store._
import org.wa9nnn.fdcluster.store.network.cluster.ClusterState
import org.wa9nnn.fdcluster.store.network.{MultcastSenderActor, MulticastListener}
import org.wa9nnn.util._

import javax.inject.{Named, Singleton}

/**
 * This is where dependency injection (Guice) is managed.
 * Note not all objects are specifically configured here. Many, (most) are simply annotated with @Inject() and
 * scala guice magic does automatically adds them as required.
 *
 * @param parameters command line args
 */
class Module(parameters: Parameters) extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    try {
      val config:Config= ConfigApp.apply
      LogFilePath(config)
      // File manager must be invoked before any logging is done as logback.xml uses the system property  "log.file.path"
      // which gets set by th3 FileManager.
      bind[CommandLine].toInstance(new CommandLineScalaFxImpl(parameters))
      bind[MulticastListener].asEagerSingleton()
      val actorSystem = ActorSystem("default", config)
      val deadLetterMonitorActor =
        actorSystem.actorOf(Props[DeadLetterMonitorActor],
          name = "deadlettermonitoractor")
      actorSystem.eventStream.subscribe(
        deadLetterMonitorActor, classOf[DeadLetter])
      bind[QsoSource].to[StoreLogic]
      bind[NodeAddress]
        .toInstance(NodeAddress.apply(config))
      bind[Persistence]
        .to[PersistenceImpl]
        .asEagerSingleton()
      bind[ObjectProperty[QsoMetadata]]
        .annotatedWithName("qsoMetadata")
        .toInstance(ObjectProperty(QsoMetadata()))
      bind[ObjectProperty[CurrentStation]]
        .annotatedWithName("currentStation")
        .toInstance(ObjectProperty(CurrentStation()))
      bind[JournalPropertyWriting].to[JournalProperty]
      val runningTaskPane = new RunningTaskPane
      bind[RunningTaskPane].toInstance(runningTaskPane)
      bind[RunningTaskInfoConsumer].toInstance(runningTaskPane)
      bind[ActorSystem].toInstance(actorSystem)
      bind[Config].toInstance(config)
      install(TypesafeConfigModule.fromConfigWithPackage(config, "org.wa9nnn"))
      bind[MetricsReporter].asEagerSingleton()

      val qsoListeners = ScalaMultibinder.newSetBinder[AddQsoListener](binder)
      qsoListeners.addBinding.to[StatsPane]
      qsoListeners.addBinding.to[QsoCountCollector]
    }
    catch {
      case e: Throwable ⇒
        e.printStackTrace()
    }
  }


  @Provides
  @Singleton
  @Named("store")
  def storeActor(actorSystem: ActorSystem, injector: Injector): ActorRef = {
    actorSystem.actorOf(Props(new StoreActor(injector)),
      "store")
  }

  @Provides
  @Singleton
  @Named("cluster")
  def clusterStoreActor(actorSystem: ActorSystem,
                        nodeAddress: NodeAddress,
                        @Named("store") storeActor: ActorRef,
                        @Named("nodeStatusQueue") nodestatusQueue: ActorRef,
                        clusterState: ClusterState,
                        contestProperty: ContestProperty
                       ): ActorRef = {
    actorSystem.actorOf(Props(
      new ClusterActor(nodeAddress, storeActor, nodestatusQueue, clusterState, contestProperty)),
      "cluster")
  }

  @Provides
  @Singleton
  @Named("multicastSender")
  def clusterStoreActor(actorSystem: ActorSystem,
                        config: Config
                       ): ActorRef = {
    actorSystem.actorOf(Props(
      new MultcastSenderActor(config)),
      "multicastSender")
  }

  @Provides
  @Singleton
  @Named("nodeStatusQueue")
  def clusterStoreActor(actorSystem: ActorSystem): ActorRef = {
    actorSystem.actorOf(Props(new NodeStatusQueueActor()),
      "nodeStatusQueue")
  }


}
