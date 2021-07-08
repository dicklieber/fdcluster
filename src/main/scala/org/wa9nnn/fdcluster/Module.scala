
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

import _root_.scalafx.beans.property.ObjectProperty
import _root_.scalafx.collections.ObservableBuffer
import akka.actor.{ActorRef, ActorSystem, DeadLetter, Props}
import com.github.racc.tscg.TypesafeConfigModule
import com.google.inject.{AbstractModule, Injector, Provides}
import configs.Config
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}
import org.wa9nnn.fdcluster.contest.JournalProperty
import org.wa9nnn.fdcluster.javafx.cluster.{ClusterTable, FdHours, NodeHistory}
import org.wa9nnn.fdcluster.javafx.entry.{RunningTaskInfoConsumer, RunningTaskPane, StatsPane}
import org.wa9nnn.fdcluster.metrics.MetricsReporter
import org.wa9nnn.fdcluster.model._
import org.wa9nnn.fdcluster.model.sync.{ClusterActor, NodeStatusQueueActor}
import org.wa9nnn.fdcluster.store._
import org.wa9nnn.util._
import org.wa9nnn.webclient.SessionManager

import javax.inject.{Named, Singleton}

/**
 * This is where dependency injection (Guice) is managed.
 * Note not all objects are specifically configured here. Many, (most) are simply annotated with @Inject() and
 * scala guice magic does automatically adds them as required.
 *
 * @param parameters command line args
 */
class Module() extends AbstractModule with ScalaModule {
//class Module(parameters: Parameters) extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    try {
      val fileManager = new FileContext()
      bind[FileContext].toInstance(fileManager)
      // File manager must be invoked before any logging is done as logback.xml uses the system property  "log.file.path"
      // which gets set by the FileManager.
      val config: Config = ConfigApp.apply
//      bind[CommandLine].toInstance(new CommandLineScalaFxImpl(parameters))
      //      bind[MulticastListener].asEagerSingleton()
      val actorSystem = ActorSystem("default", config)
      val deadLetterMonitorActor =
        actorSystem.actorOf(Props[DeadLetterMonitorActor],
          name = "deadlettermonitoractor")
      actorSystem.eventStream.subscribe(
        deadLetterMonitorActor, classOf[DeadLetter])
      bind[QsoSource].to[StoreLogic]
      bind[NodeAddress]
        .toInstance(NodeAddress(fileManager.instance, config))
      bind[Persistence]
        .to[PersistenceImpl]
        .asEagerSingleton()
      bind[ObjectProperty[Station]]
        .annotatedWithName("currentStation")
        .toInstance(ObjectProperty(Station()))
      val runningTaskPane = new RunningTaskPane
      bind[ObservableBuffer[Qso]].to[QsoBuffer]
      bind[RunningTaskPane].toInstance(runningTaskPane)
      bind[RunningTaskInfoConsumer].toInstance(runningTaskPane)
      bind[ActorSystem].toInstance(actorSystem)
      bind[Config].toInstance(config)
      install(TypesafeConfigModule.fromConfigWithPackage(config, "org.wa9nnn"))
      bind[MetricsReporter].asEagerSingleton()
      bind[QsoBuilder].to[OsoMetadataProperty]
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
  @Named("sessionManager")
  def sessionManagerActor(actorSystem: ActorSystem,
                          config: Config): ActorRef = {
    actorSystem.actorOf(Props(new SessionManager(config)),
      "sessionManager")
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
                        contestProperty: ContestProperty,
                        journalProperty: JournalProperty,
                        clusterTable: ClusterTable,
                        fdHours: FdHours,
                        nodeHistory: NodeHistory
                       ): ActorRef = {
    actorSystem.actorOf(Props(
      new ClusterActor(nodeAddress, storeActor, nodestatusQueue, contestProperty, journalProperty, clusterTable, fdHours, nodeHistory)),
      "cluster")
  }


  @Provides
  @Singleton
  @Named("nodeStatusQueue")
  def clusterStoreActor(actorSystem: ActorSystem): ActorRef = {
    actorSystem.actorOf(Props(new NodeStatusQueueActor()),
      "nodeStatusQueue")
  }

}
